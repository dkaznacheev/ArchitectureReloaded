/*
 * Copyright 2005-2017 Sixth and Red River Software, Bas Leijdekkers
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.sixrr.stockmetrics.methodMetrics;

import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricType;
import com.sixrr.stockmetrics.i18n.HelpURLs;
import com.sixrr.stockmetrics.i18n.StockMetricsBundle;
import com.sixrr.stockmetrics.methodCalculators.ConditionCountCalculator;
import com.sixrr.stockmetrics.methodCalculators.HelsteadProgramLevelCalculator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HalsteadProgramLevelMetric extends MethodMetric {

    @NotNull
    @Override
    public String getDisplayName() {
        return StockMetricsBundle.message("halstead.program.level.method.metric.display.name");
    }

    @NotNull
    @Override
    public String getAbbreviation() {
        return StockMetricsBundle.message("halstead.program.level.method.metric.abbreviation");
    }

    @NotNull
    @Override
    public MetricType getType() {
        return MetricType.Score;
    }

    @NotNull
    @Override
    public MetricCalculator createCalculator() {
        return new HelsteadProgramLevelCalculator();
    }

    @Nullable
    @Override
    public String getHelpURL() {
        return HelpURLs.HALSTEAD_LEVEL_URL;
    }

    @Nullable
    @Override
    public String getHelpDisplayString() {
        return HelpURLs.HALSTEAD_LEVEL_DISPLAY_STRING;
    }
}
