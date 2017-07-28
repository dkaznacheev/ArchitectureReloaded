/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ml_methods_group.plugin;

import com.intellij.analysis.AnalysisScope;
import com.intellij.analysis.BaseAnalysisAction;
import com.intellij.analysis.BaseAnalysisActionDialog;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.ui.dialogs.ProfileSelectionPanel;
import com.sixrr.metrics.ui.metricdisplay.MetricsToolWindow;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.AlgorithmResult;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.config.ArchitectureReloadedConfig;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.refactoring.RefactoringExecutionContext;
import org.ml_methods_group.ui.AlgorithmsSelectionPanel;
import org.ml_methods_group.ui.RefactoringsToolWindow;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;
import org.ml_methods_group.utils.MetricsProfilesUtil;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AutomaticRefactoringAction extends BaseAnalysisAction {
    private static final Logger LOGGER = Logging.getLogger(AutomaticRefactoringAction.class);
    private static final String REFACTORING_PROFILE_KEY = "refactoring.metrics.profile.name";

    private Map<String, AlgorithmResult> results = new HashMap<>();

    private static final Map<Project, AutomaticRefactoringAction> factory = new HashMap<>();

    private static ProjectManagerListener listener = new ProjectManagerListener() {
        @Override
        public void projectOpened(Project project) {
            getInstance(project).analyzeBackground(project, new AnalysisScope(project));
        }

        @Override
        public void projectClosed(Project project) {
            deleteInstance(project);
        }
    };

    static {
        // todo fix bug: IndexNotReadyException
//        ProjectManager.getInstance().addProjectManagerListener(listener);
    }

    public AutomaticRefactoringAction() {
        super(MetricsReloadedBundle.message("metrics.calculation"), MetricsReloadedBundle.message("metrics"));
    }

    @NotNull
    public static AutomaticRefactoringAction getInstance(@NotNull Project project) {
        if (!factory.containsKey(project)) {
            PluginManager.getLogger().info("Creating refactoring action for project " + project.getName());
            factory.put(project, new AutomaticRefactoringAction());
        }
        return factory.get(project);
    }

    private static void deleteInstance(@NotNull Project project) {
        factory.remove(project);
    }

    @Override
    protected void analyze(@NotNull final Project project, @NotNull final AnalysisScope analysisScope) {
        LOGGER.info("Run analysis (scope=" + analysisScope.getDisplayName() + ")");

        final MetricsProfile metricsProfile = MetricsProfileRepository.getInstance()
                .getCurrentProfile();
        assert metricsProfile != null;
        final Collection<String> selectedAlgorithms = ArchitectureReloadedConfig.getInstance().getSelectedAlgorithms();
        new RefactoringExecutionContext(project, analysisScope, metricsProfile, selectedAlgorithms, this::showDialogs)
                .executeAsync();
    }

    public void analyzeBackground(@NotNull final Project project, @NotNull final AnalysisScope analysisScope) {
        checkRefactoringProfile();
        final MetricsProfile metricsProfile = MetricsProfileRepository.getInstance()
                .getProfileForName(ArchitectureReloadedBundle.message(REFACTORING_PROFILE_KEY));
        assert metricsProfile != null;
        final RefactoringExecutionContext context =
                new RefactoringExecutionContext(project, analysisScope, metricsProfile, this::updateResults);
        new Task.Backgroundable(project,
                "Calculating Refactorings...", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                context.executeSynchronously();
            }

            @Override
            public void onFinished() {
                super.onFinished();
                DaemonCodeAnalyzer.getInstance(project).restart();
            }

        }.queue();
    }

    private void updateResults(@NotNull RefactoringExecutionContext context) {
        for (AlgorithmResult result : context.getAlgorithmResults()) {
            results.put(result.getAlgorithmName(), result);
        }
    }


    private void showDialogs(@NotNull RefactoringExecutionContext context) {
        updateResults(context);
        final Set<String> selectedAlgorithms = ArchitectureReloadedConfig.getInstance().getSelectedAlgorithms();
        final List<AlgorithmResult> algorithmResult = selectedAlgorithms.stream()
                .map(results::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        ServiceManager.getService(context.getProject(), MetricsToolWindow.class)
                .show(context.getMetricsRun(), context.getProfile(), context.getScope(), false);
        ServiceManager.getService(context.getProject(), RefactoringsToolWindow.class)
                .show(algorithmResult, context.getEntitySearchResult(), context.getScope());
    }

    private static void checkRefactoringProfile() {
        final Set<Class<? extends Metric>> requestedSet = Entity.getRequestedMetrics();
        final String profileName = ArchitectureReloadedBundle.message(REFACTORING_PROFILE_KEY);
        final MetricsProfileRepository repository = MetricsProfileRepository.getInstance();
        if (MetricsProfilesUtil.checkMetricsList(profileName, requestedSet, repository)) {
            return;
        }
        MetricsProfilesUtil.removeProfileForName(profileName, repository);
        repository.addProfile(MetricsProfilesUtil.createProfile(profileName, requestedSet));
    }

    @NotNull
    public Set<String> calculatedAlgorithms() {
        return results.keySet();
    }

    @NotNull
    public Map<String, String> getRefactoringsForName(String algorithm) {
        if (!results.containsKey(algorithm)) {
            throw new IllegalArgumentException("Uncalculated algorithm requested: " + algorithm);
        }
        return results.get(algorithm).getRefactorings();
    }

    @Override
    @Nullable
    protected JComponent getAdditionalActionSettings(Project project, BaseAnalysisActionDialog dialog) {
        checkRefactoringProfile();
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(new ProfileSelectionPanel(project), BorderLayout.SOUTH);
        panel.add(new AlgorithmsSelectionPanel(), BorderLayout.NORTH);
        return panel;
    }
}
