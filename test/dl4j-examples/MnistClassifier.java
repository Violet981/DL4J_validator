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

package ai.certifai.solution.convolution.mnist;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * Hello World with Mnist DataSet
 *
 ******************************************************
 * Look for LAB STEP below. Uncomment to proceed.
 *
 * 1. Set up Model configuration
 * 2. Test on a single image
 * *****************************************************
 */
public class MnistClassifier{

    private static final int channels = 1; // single channel for grayscale images
    private static final int outputNum = 10; // 10 digits classification
    private static final int batchSize = 54;
    private static final int nEpochs = 1;
    private static final double learningRate = 0.001;
    private static MultiLayerNetwork model = null;

    private static final int seed = 1234;

    public static void main(String[] args) throws Exception
    {
    /*
    Create an iterator using the batch size for one iteration
    */
        DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize,true, seed);
        DataSetIterator mnistTest = new MnistDataSetIterator(batchSize,false, seed);


    /*
   Model configuration
    */
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new Nesterovs(learningRate, Nesterovs.DEFAULT_NESTEROV_MOMENTUM))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new ConvolutionLayer.Builder(5, 5)
                        .nIn(channels)
                        .stride(1, 1)
                        .nOut(20)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(2, new DenseLayer.Builder().activation(Activation.RELU)
                        .nOut(50).build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(outputNum)
                        .activation(Activation.SOFTMAX)
                        .build())
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));

        // evaluation while training (the score should go down)
        for (int i = 0; i < nEpochs; i++) {
            model.fit(mnistTrain);

            Evaluation eval = model.evaluate(mnistTest);
            mnistTrain.reset();
            mnistTest.reset();
        }

    }

}
