/*
 * Copyright (C) 2012 SeqWare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.seqware.queryengine.plugins.inmemory;

import com.github.seqware.queryengine.factory.CreateUpdateManager;
import com.github.seqware.queryengine.factory.SWQEFactory;
import com.github.seqware.queryengine.kernel.RPNStack;
import com.github.seqware.queryengine.model.Feature;
import com.github.seqware.queryengine.model.FeatureSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Generic query implementation over all attributes of a Feature (including additional attributes).
 *
 * @author dyuen
 * @author jbaran
 */
public class InMemoryFeaturesByAttributesPlugin extends AbstractMRInMemoryPlugin {

    private RPNStack rpnStack;
    private Set<Feature> accumulator = new HashSet<Feature>();

    @Override
    public ReturnValue init(FeatureSet inputSet, Object ... parameters) {
        this.inputSet = inputSet;
        this.rpnStack = (RPNStack)parameters[0];
        return new ReturnValue();
    }

    @Override
    public ReturnValue map(Feature feature, FeatureSet mappedSet) {
        boolean result = matchFeature(feature, rpnStack);

        // Now carry out the actual evaluation that determines whether f is relevant:
        if (result){
            Feature build = feature.toBuilder().build();
            accumulator.add(build);
        }

        return new ReturnValue();
    }

    public static boolean matchFeature(Feature feature, RPNStack rpnStack) {
        // Get the parameters from the RPN stack and replace them with concrete values:
        for (Object parameter : rpnStack.getParameters()){
            rpnStack.setParameter(parameter, feature.getAttribute((String)parameter));
        }
        boolean result = (Boolean)rpnStack.evaluate();
        return result;
    }

    @Override
    public ReturnValue reduce(FeatureSet mappedSet, FeatureSet resultSet) {
        // doesn't really do anything
        return new ReturnValue();
    }

    @Override
    public FeatureSet getFinalResult() {
        super.performInMemoryRun();
        CreateUpdateManager mManager = SWQEFactory.getModelManager();
        FeatureSet fSet = mManager.buildFeatureSet().setReference(mManager.buildReference().setName("ad_hoc_analysis").build()).build();
        for(Feature f : accumulator){
            mManager.objectCreated(f);
        }
        fSet.add(accumulator);
        mManager.close();
        return fSet;
    }

    @Override
    public ReturnValue reduceInit() {
        // doesn't really do anything
        return new ReturnValue();
    }

    @Override
    public ReturnValue mapInit() {
        // doesn't really do anything
        return new ReturnValue();
    }
}