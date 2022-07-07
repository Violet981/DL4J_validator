/* *****************************************************************************
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

package org.deeplearning4j.datapipelineexamples.transform.basic;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datapipelineexamples.utils.DownloaderUtility;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;

import java.io.File;

/**
 * This basic example demonstrates how to use the preprocessors available
 * This example uses the minmax scaler and will work with the 3.10 release and later
 * Created by susaneraly on 6/8/16.
 */
public class IrisNormalizer {

    public static void main(String[] args) throws  Exception {


        //========= This section is to create a dataset and a dataset iterator from the iris dataset stored in csv =============
        //                               Refer to the csv example for details
        int numLinesToSkip = 0;
        char delimiter = ',';
        String localDataPath = "path/to/data";
        RecordReader recordReader = new CSVRecordReader(numLinesToSkip,delimiter);
        RecordReader recordReaderA = new CSVRecordReader(numLinesToSkip,delimiter);
        RecordReader recordReaderB = new CSVRecordReader(numLinesToSkip,delimiter);
        recordReader.initialize(new FileSplit(new File(localDataPath,"iris.txt")));
        recordReaderA.initialize(new FileSplit(new File(localDataPath,"iris.txt")));
        recordReaderB.initialize(new FileSplit(new File(localDataPath,"iris.txt")));
        int labelIndex = 4;
        int numClasses = 3;
        DataSetIterator iteratorA = new RecordReaderDataSetIterator(recordReaderA,10,labelIndex,numClasses);
        DataSetIterator iteratorB = new RecordReaderDataSetIterator(recordReaderB,10,labelIndex,numClasses);
        DataSetIterator fulliterator = new RecordReaderDataSetIterator(recordReader,150,labelIndex,numClasses);
        DataSet datasetX = fulliterator.next();
        DataSet datasetY = datasetX.copy();

        // We now have datasetX, datasetY, iteratorA, iteratorB all of which have the iris dataset loaded
        // iteratorA and iteratorB have batchsize of 10. So the full dataset is 150/10 = 15 batches
        //=====================================================================================================================

        NormalizerMinMaxScaler preProcessor = new NormalizerMinMaxScaler();

        //Fitting a preprocessor with a dataset
        preProcessor.fit(datasetX);

        preProcessor.transform(datasetX);
        preProcessor.revert(datasetX);

        //Setting a preprocessor in an iterator
        NormalizerMinMaxScaler preProcessorIter = new NormalizerMinMaxScaler();
        preProcessorIter.fit(iteratorB);

        iteratorA.setPreProcessor(preProcessorIter);
        while (iteratorA.hasNext()) {
            DataSet firstBatch = iteratorB.next();
            iteratorB.reset();
            firstBatch = iteratorB.next();
            preProcessorIter.transform(firstBatch);
        }

        NormalizerMinMaxScaler preProcessorRange = new NormalizerMinMaxScaler(-1,1);

        preProcessorRange.fit(datasetY);

        preProcessorRange.transform(datasetY);

    }
}
