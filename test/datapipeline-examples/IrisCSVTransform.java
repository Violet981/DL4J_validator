/*******************************************************************************
 * Copyright (c) 2020 Konduit K.K.
 * Copyright (c) 2015-2019 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.writable.Writable;
import org.datavec.api.split.FileSplit;
import org.datavec.local.transforms.LocalTransformExecutor;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

public class IrisCSVTransform {

    public static  void main(String[] args) throws Exception {

        String filename = "iris.data";

        File irisText = new File(filename);

        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(irisText));

        //Process the data:
        List<List<Writable>> originalData = new ArrayList<>();
        while(rr.hasNext()){
            originalData.add(rr.next());
        }

        // the String to label conversion. Define schema and transform:
        Schema schema = new Schema.Builder()
            .addColumnsDouble("Sepal length", "Sepal width", "Petal length", "Petal width")
            .addColumnCategorical("Species", "Iris-setosa", "Iris-versicolor", "Iris-virginica")
            .build();

        TransformProcess tp = new TransformProcess.Builder(schema)
            .categoricalToInteger("Species")
            .build();

        List<List<Writable>> processedData = LocalTransformExecutor.execute(originalData, tp);
        System.out.println(processedData);

    }
}
