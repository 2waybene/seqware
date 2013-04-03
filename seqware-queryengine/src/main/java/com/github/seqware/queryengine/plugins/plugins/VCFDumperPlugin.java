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
package com.github.seqware.queryengine.plugins.plugins;

import com.github.seqware.queryengine.model.Feature;
import com.github.seqware.queryengine.plugins.MapReducePlugin;
import com.github.seqware.queryengine.plugins.MapperInterface;
import com.github.seqware.queryengine.plugins.ReducerInterface;
import com.github.seqware.queryengine.system.exporters.VCFDumper;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import org.apache.commons.lang.SerializationUtils;
import org.apache.hadoop.io.Text;

/**
 * This plug-in implements a quick and dirty export using Map/Reduce
 *
 * TODO: Copy from HDFS and parse key value file to VCF properly.
 *
 * @author dyuen
 * @version $Id: $Id
 */
public class VCFDumperPlugin extends MapReducePlugin<Text, Text, Text, Text, Text, Text, File> implements Serializable {

    private Text text = new Text();
    private Text textKey = new Text();
    
    @Override
    public Class getMapOutputKeyClass() {
        return Text.class;
    }

    @Override
    public Class getMapOutputValueClass() {
        return Text.class;
    }

    @Override
    public int getNumReduceTasks() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getInternalParameters() {
        return new Object[0];
    }

    @Override
    public void map(Collection<Feature> collection, MapperInterface<Text, Text> mapperInterface) {
        for (Feature f : collection) {
            StringBuffer buffer = new StringBuffer();
            VCFDumper.outputFeatureInVCF(buffer, f);
            text.set(buffer.toString());     // we can only emit Writables...
            textKey.set(f.getSGID().getRowKey());
            mapperInterface.write(textKey, text);
        }
    }

    @Override
    public void reduce(Text key, Iterable<Text> values, ReducerInterface<Text, Text> reducerInterface) {
        for (Text val : values) {
            reducerInterface.write(val, text);
        }
    }

    @Override
    public ResultMechanism getResultMechanism() {
        return ResultMechanism.FILE;
    }

    @Override
    public Class<?> getResultClass() {
        return File.class;
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException{
        if (text == null){
            text = new Text();
        }
        if (textKey == null){
            textKey = new Text();
        }
        text.readFields(in);
        textKey.readFields(in);
    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        text.write(out);
        textKey.write(out);
    }
    
    public static void main (String[] args){
        VCFDumperPlugin plugin = new VCFDumperPlugin();
        byte[] bytes = SerializationUtils.serialize(plugin);
        VCFDumperPlugin plugin2 = (VCFDumperPlugin) SerializationUtils.deserialize(bytes);
    }
}
