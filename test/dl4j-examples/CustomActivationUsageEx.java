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


import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.dataset.DataSet;

import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.activations.BaseActivationFunction;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.transforms.strict.TanDerivative;
import org.nd4j.linalg.api.ops.impl.transforms.strict.Tanh;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This is an example that illustrates how to instantiate and use a custom activation function.
 * The example is identical to the one in org.deeplearning4j.examples.feedforward.regression.RegressionSum
 * except for the custom activation function
 */


public class CustomActivationUsageEx {

    class CustomActivationDefinition extends BaseActivationFunction{

        /*
            For the forward pass:

            Transform "in" with the activation function. Best practice is to do the transform in place as shown below
            Can support different behaviour during training and test with the boolean argument
         */
        @Override
        public INDArray getActivation(INDArray in, boolean training) {
            //Modify array "in" inplace to transform it with the activation function
            // h(x) = 1.7159*tanh(2x/3)
            Nd4j.getExecutioner().execAndReturn(new Tanh(in.muli(2/3.0)));
            in.muli(1.7159);
            return in;
        }

        /*
            For the backward pass:
            Given epsilon, the gradient at every activation node calculate the next set of gradients for the backward pass
            Best practice is to modify in place.

            Using the terminology,
                in -> linear input to the activation node
                out    -> the output of the activation node, or in other words h(out) where h is the activation function
                epsilon -> the gradient of the loss function with respect to the output of the activation node, d(Loss)/dout

                    h(in) = out;
                    d(Loss)/d(in) = d(Loss)/d(out) * d(out)/d(in)
                                  = epsilon * h'(in)
         */
        @Override
        public Pair<INDArray,INDArray> backprop(INDArray in, INDArray epsilon) {
            //dldZ here is h'(in) in the description above
            //
            //      h(x) = 1.7159*tanh(2x/3);
            //      h'(x) = 1.7159*[tanh(2x/3)]' * 2/3
            INDArray dLdz = Nd4j.getExecutioner().exec(new TanDerivative(in.muli(2/3.0)));
            dLdz.muli(2/3.0);
            dLdz.muli(1.7159);

            //Multiply with epsilon
            dLdz.muli(epsilon);
            return new Pair<>(dLdz, null);
        }

    }


    public static DataSet getTrainingData(int batchSize, Random rand){
        int nSamples = 1000;
        double [] sum = new double[nSamples];
        double [] input1 = new double[nSamples];
        double [] input2 = new double[nSamples];
        for (int i= 0; i< nSamples; i++) {
            input1[i] = MIN_RANGE + (MAX_RANGE - MIN_RANGE) * rand.nextDouble();
            input2[i] =  MIN_RANGE + (MAX_RANGE - MIN_RANGE) * rand.nextDouble();
            sum[i] = input1[i] + input2[i];
        }
        INDArray inputNDArray1 = Nd4j.create(input1);
        INDArray inputNDArray2 = Nd4j.create(input2);
        INDArray inputNDArray = Nd4j.hstack(inputNDArray1,inputNDArray2);
        INDArray outPut = Nd4j.create(sum);
        DataSet dataSet = new DataSet(inputNDArray, outPut);

        return dataSet;

    }

    public static void main(String[] args) {

        DataSet ds = getTrainingData(100, new Random(1234));

        //Create the network
        int numInput = 2;
        int numOutputs = 1;
        int nHidden = 10;
        MultiLayerNetwork net = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(1234)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(0.001, 0.9))
                .list()
                //INSTANTIATING CUSTOM ACTIVATION FUNCTION here as follows
                //Refer to CustomActivation class for more details on implementation
                .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(nHidden)
                        .activation(new CustomActivationDefinition())
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(nHidden).nOut(numOutputs).build())
                .build()
        );
        net.init();
        net.setListeners(new ScoreIterationListener(100));
        net.fit(ds, 10);
        System.out.println("Training complete");

    }
}
