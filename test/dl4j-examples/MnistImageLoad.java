/*
 * Copyright (c) 2020-2021 CertifAI Sdn. Bhd.
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
 *
 */

package ai.certifai.solution.modelsaveload;

import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.canova.RecordReaderDataSetIterator;



import java.io.File;

/**

 *  This examples builds on the MnistImagePipelineExample
 *  by loading the trained network
 *
 * To run this sample, you must have
 * (1) save trained mnist model
 * (2) test image
 *
 *  Look for LAB STEP below. Uncomment to proceed.
 *  1. Load the saved model
 *  2. Load an image for testing
 *  3. [Optional] Preprocessing to 0-1 or 0-255
 *  4. Pass through the neural net for prediction
 */

public class MnistImageLoad
{

    public static void main(String[] args) throws Exception
    {
        // image information
        // 28 * 28 grayscale
        // grayscale implies single channel
        int height = 28;
        int width = 28;
        int channels = 1;


        File modelSave =  new File(System.getProperty("java.io.tmpdir"), "/trained_mnist_model.zip");

        if(modelSave.exists() == false)
        {
            System.out.println("Model not exist. Abort");
            return;
        }
        String imagePath = "image/five.png";

        /*
		#### LAB STEP 1 #####
		Load the saved model
        */
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(modelSave);

        /*
		#### LAB STEP 2 #####
		Load an image for testing

        */
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        // Use NativeImageLoader to convert to numerical matrix
        ImageRecordReader recordReader = new ImageRecordReader(height,width,channels,labelMaker);


        /*
		#### LAB STEP 3 #####
        */

        recordReader.initialize(new FileSplit(new File(imagePath)));

        DataSetIterator iter = new RecordReaderDataSetIterator(recordReader, 784*3,3, 10);


        /*
		#### LAB STEP 4 #####
		[Optional] Pass to the neural net for prediction
        */

        INDArray output = model.output(iter);

    }

}
