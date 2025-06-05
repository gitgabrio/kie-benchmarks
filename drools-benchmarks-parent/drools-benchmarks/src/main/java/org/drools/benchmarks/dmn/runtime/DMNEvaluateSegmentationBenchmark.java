/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.drools.benchmarks.dmn.runtime;

import org.drools.benchmarks.common.AbstractBenchmark;
import org.drools.benchmarks.common.ProviderException;
import org.drools.benchmarks.dmn.runtime.model.Segment;
import org.drools.benchmarks.dmn.util.DMNUtil;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Warmup;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Warmup(iterations = 300)
@Measurement(iterations = 50)
@Fork(0)
public class DMNEvaluateSegmentationBenchmark extends AbstractBenchmark {

    @Param({"1000"})
    private int numberOfDecisions;

    private Resource dmnResource;
    private DMNRuntime dmnRuntime;
    private DMNModel dmnModel;
    private DMNContext dmnContext;


    public static void main(String[] args) throws Exception {
        DMNEvaluateSegmentationBenchmark instance = new DMNEvaluateSegmentationBenchmark();
        instance.setupResource();
        instance.setup();
        instance.evaluateDecision();
    }

    @Setup
    public void setupResource() throws IOException {
        dmnResource = KieServices.get().getResources()
                .newClassPathResource("dmn/segmentation_fixed.dmn");
        dmnRuntime = DMNUtil.getDMNRuntimeWithResources(false, dmnResource);
        dmnModel = dmnRuntime.getModel("https://kiegroup.org/dmn/_3C914477-1CBC-46AA-A53B-3774FA1BFDE9", "WeatherAdvice");
    }

    @Setup(Level.Iteration)
    @Override
    public void setup() throws ProviderException {
        dmnContext = dmnRuntime.newContext();
        Map<String, Object> consumer = new HashMap<>();
        consumer.put("age", 22);
        consumer.put("monthlypreferredmsgfrequency", 20);
        consumer.put("prodstyleofinterest", "trendy");
        consumer.put("prodcatofinterest1", "computers");
        consumer.put("state", "AA");
        consumer.put("incomegroupcode", 6);
        consumer.put("createdon", LocalDate.of(2005, Month.JANUARY, 2));
        dmnContext.set("consumer", consumer);
        Map<String, Object> campaign = new HashMap<>();
        List<Segment> segments = new ArrayList<>();
        IntStream.range(0, 5).forEach(i -> {
            org.drools.benchmarks.dmn.runtime.model.Segment toAdd = new org.drools.benchmarks.dmn.runtime.model.Segment();
            toAdd.setId(i);
            toAdd.setScore(0);
            segments.add(toAdd);
        });
        campaign.put("segments", segments);
        dmnContext.set("campaign", campaign);
    }

    @Benchmark
    public DMNResult evaluateDecision() {
        return dmnRuntime.evaluateAll(dmnModel, dmnContext);
    }

}
