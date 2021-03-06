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

import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.schema.Schema;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Mulitple operations on a single column
 *
 * @author François Garillot
 */

public class MultiOpReduceExample {

    public static  void main(String[] args) throws Exception {

        //=====================================================================
        //                 Step 1: Define the input data schema as in the Basic Example
        //=====================================================================

        //Let's define the schema of the data that we want to import
        //The order in which columns are defined here should match the order in which they appear in the input data
        Schema inputDataSchema = new Schema.Builder()
            .addColumnString("DateTimeString")
            .addColumnsString("CustomerID", "MerchantID")
            .addColumnInteger("NumItemsInTransaction")
            .addColumnCategorical("MerchantCountryCode", Arrays.asList("USA","CAN","FR","MX"))
            //Some columns have restrictions on the allowable values, that we consider valid:
            .addColumnDouble("TransactionAmountUSD",0.0,null,false,false)   //$0.0 or more, no maximum limit, no NaN and no Infinite values
            .addColumnCategorical("FraudLabel", Arrays.asList("Fraud","Legit"))
            .build();



        //Lets define some operations to execute on the data...
        //We do this by defining a TransformProcess
        //At each step, we identify column by the name we gave them in the input data schema, above

        TransformProcess tp = new TransformProcess.Builder(inputDataSchema)
            //Let's remove some column we don't need
            .removeColumns("CustomerID","MerchantID", "MerchantCountryCode", "FraudLabel")

            //However, our time column ("DateTimeString") isn't a String anymore. So let's rename it to something better:
            .renameColumn("DateTimeString", "DateTime")

            //We've finished with the sequence of operations we want to do: let's create the final TransformProcess object
            .build();


        //After executing all of these operations, we have a new and different schema:
        Schema outputSchema = tp.getFinalSchema();

        System.out.println("\n\n\nSchema after transforming data:");
        System.out.println(outputSchema);

    }

}
