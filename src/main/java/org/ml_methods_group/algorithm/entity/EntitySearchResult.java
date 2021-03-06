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

package org.ml_methods_group.algorithm.entity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class EntitySearchResult {
    private final List<ClassEntity> classes;
    private final List<MethodEntity> methods;
    private final List<FieldEntity> fields;
    private final int propertiesCount;
    private final long searchTime;

    public EntitySearchResult(List<ClassEntity> classes, List<MethodEntity> methods, List<FieldEntity> fields,
                              long searchTime) {
        this.classes = classes;
        this.methods = methods;
        this.fields = fields;
        this.searchTime = searchTime;
        propertiesCount = Stream.of(classes, methods, fields)
                .flatMap(List::stream)
                .map(Entity::getRelevantProperties)
                .mapToInt(RelevantProperties::size)
                .sum();
    }

    public List<ClassEntity> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    public List<MethodEntity> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    public List<FieldEntity> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public int getPropertiesCount() {
        return propertiesCount;
    }

    public long getSearchTime() {
        return searchTime;
    }
}
