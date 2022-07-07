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

package org.deeplearning4j.datapipelineexamples.transform.debugging;

import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.condition.ConditionOp;
import org.datavec.api.transform.condition.column.CategoricalColumnCondition;
import org.datavec.api.transform.filter.ConditionFilter;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.transform.transform.time.DeriveColumnsFromTimeTransform;
import org.datavec.api.writable.DoubleWritable;

import java.util.Arrays;
import java.util.HashSet;

/**
 * This is a simple example for the DataVec transformation functionality (building on BasicDataVecExample)
 * It is designed to simply demonstrate that it is possible to obtain the schema after each step of a transform process.
 * This can be useful for debugging your TransformProcess scripts.
 *
 * @author Alex Black
 */
public class PrintSchemasAtEachStep {

    public static void main(String[] args){

        //Define the Schema and TransformProcess as per BasicDataVecExample
        Schema inputDataSchema = new Schema.Builder()
            .addColumnsString("DateTimeString", "CustomerID", "MerchantID")
            .addColumnInteger("NumItemsInTransaction")
            .addColumnCategorical("MerchantCountryCode", Arrays.asList("USA","CAN","FR","MX"))
            .addColumnDouble("TransactionAmountUSD",0.0,null,false,false)   //$0.0 or more, no maximum limit, no NaN and no Infinite values
            .addColumnCategorical("FraudLabel", Arrays.asList("Fraud","Legit"))
            .build();

        TransformProcess tp = new TransformProcess.Builder(inputDataSchema)
            .removeColumns("CustomerID","MerchantID")
            .filter(new ConditionFilter(new CategoricalColumnCondition("MerchantCountryCode", ConditionOp.NotInSet, new HashSet<>(Arrays.asList("USA","CAN")))))
            .renameColumn("DateTimeString", "DateTime")
            .removeColumns("DateTime")
            .build();

    }

}
