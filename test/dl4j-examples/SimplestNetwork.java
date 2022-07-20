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

package ai.certifai.solution.feedforward;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;


/**
 * Simplest Network - Predicting an output value from an input value
 * 0.5 -> 0.8
 */

public class SimplestNetwork
{

    public static void main(String[] args) throws Exception
    {

        int seed = 123; // consistent Random Numbers needed for testing. Initial weights are randomized
        int epochs = 50; // Number of epochs(full passes of the data)
        double learningRate = 0.001; //How fast to adjust weights to minimize error
        int numInputs = 1; // number of input nodes
        int numOutputs = 1; // number of output nodes
        int nHidden = 5; // number of hidden nodes


        /*
		#### LAB STEP 1 #####
		Declare the input and output data in INDArray format
        */

        INDArray input = Nd4j.create(new float[]{(float) 0.5});
        INDArray output = Nd4j.create(new float[]{(float) 0.8});


        /*
		#### LAB STEP 2 #####
		Set up the network configuration
        */
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(learningRate, 0.9))
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(numInputs)
                        .nOut(nHidden)
                        .activation(Activation.TANH)
                        .build())
                .layer(1, new OutputLayer.Builder()
                        .activation(Activation.IDENTITY)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .nIn(nHidden)
                        .nOut(numOutputs)
                        .build())
                .build();


        /*
		#### LAB STEP  #####
		Declare MultiLayerNetwork, train the network
		*/
        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();

        for(int i = 0; i < epochs; ++i)
        {
            model.fit(input, output);
            INDArray predicted = model.output(input);
        }

    }

}
