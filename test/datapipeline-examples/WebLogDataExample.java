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
import org.datavec.api.transform.condition.string.StringRegexColumnCondition;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.writable.IntWritable;
import org.datavec.api.writable.Writable;
import org.datavec.local.transforms.LocalTransformExecutor;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
/**
 *
 * @author Alex Black
 */
public class WebLogDataExample {

    public static void main(String[] args) throws Exception {
        //=====================================================================
        //                 Step 1: Define the input data schema
        //=====================================================================

        //First: let's specify a schema for the data. This is based on the information from: http://ita.ee.lbl.gov/html/contrib/NASA-HTTP.html
        Schema schema = new Schema.Builder()
            .addColumnString("host")
            .addColumnString("timestamp")
            .addColumnString("request")
            .addColumnInteger("httpReplyCode")
            .addColumnInteger("replyBytes")
            .build();

        //=====================================================================
        //         Step 2: Parse Raw Data and Perform Initial Analysis
        //=====================================================================

        String fileName = "path/to/data";
        RecordReader rr = new CSVRecordReader();
        File file = new File(fileName);
        rr.initialize(new FileSplit(file));

        //Process the data:
        List<List<Writable>> originalData = new ArrayList<>();
        while(rr.hasNext()){
            originalData.add(rr.next());
        }


        //=====================================================================
        //          Step 3: Perform Cleaning, Parsing and Aggregation
        //=====================================================================

        //Let's specify the transforms we want to do
        TransformProcess tp = new TransformProcess.Builder(schema)
            //First: clean up the "replyBytes" column by replacing any non-integer entries with the value 0
            .conditionalReplaceValueTransform("replyBytes",new IntWritable(0), new StringRegexColumnCondition("replyBytes","\\D+"))

            .renameColumn("count(timestamp)", "numRequests")

            .build();

        List<List<Writable>> processedData = LocalTransformExecutor.execute(originalData, tp);
        System.out.println(processedData);

        //=====================================================================
        //       Step 4: Display Results
        //=====================================================================
        Schema finalDataSchema = tp.getFinalSchema();
        System.out.println(finalDataSchema);
    }
}
