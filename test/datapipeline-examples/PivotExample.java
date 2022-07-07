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
import org.datavec.local.transforms.LocalTransformExecutor;
import org.datavec.api.split.FileSplit;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Alex Black
 */
public class PivotExample {

    public static void main(String[] args) throws Exception {
        String fileName = "path/to/data";

        //=====================================================================
        //                 Step 1: Define the input data schema
        //=====================================================================

        //Let's define the schema of the data that we want to import
        //The order in which columns are defined here should match the order in which they appear in the input data
        Schema inputDataSchema = new Schema.Builder()
            .addColumnString("person")
            .addColumnCategorical("country_visited", Arrays.asList("USA","Japan","China","India"))
            .addColumnString("entry_time")
            .build();

        //=====================================================================
        //            Step 2: Define the operations we want to do
        //=====================================================================

        TransformProcess tp = new TransformProcess.Builder(inputDataSchema)
            //Let's first take the "country_visited" column and expand it to a one-hot representation
            //So, "USA" becomes [1,0,0,0], "Japan" becomes [0,1,0,0], "China" becomes [0,0,1,0] etc
            .categoricalToOneHot("country_visited")

            //We can rename our columns
            .renameColumn("max(entry_time)", "most_recent_entry")

            .build();


        //=====================================================================
        //      Step 3: Load our data and execute the operations on Spark
        //=====================================================================


        //Define the path to the data file. You could use a directory here if the data is in multiple files
        RecordReader rr = new CSVRecordReader();
        File file = new File(fileName);
        rr.initialize(new FileSplit(file));

        //Process the data:
        List<List<Writable>> originalData = new ArrayList<>();
        while(rr.hasNext()){
            originalData.add(rr.next());
        }

        List<List<Writable>> processedData = LocalTransformExecutor.execute(originalData, tp);

        //Finally: we'll print the schemas and results
        System.out.println("\n\n---- Final Data Schema ----");
        Schema finalDataSchema = tp.getFinalSchema();
        System.out.println(finalDataSchema);

    }

}
