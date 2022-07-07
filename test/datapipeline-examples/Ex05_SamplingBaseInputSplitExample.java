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

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.filters.PathFilter;
import org.datavec.api.io.filters.RandomPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;

import java.io.File;
import java.util.Iterator;
import java.util.Random;

/**
 * Javaparser have problems in identifying the FileSplit.sample(PathFilter, double ...)
 * maybe because this method is inherited from its baseclass: BaseInputSplit
 * so the gt file lack the corresponding API
 */


/**
 * {@link org.datavec.api.split.BaseInputSplit} and its implementation provides a
 * {@link org.datavec.api.split.BaseInputSplit#sample(PathFilter, double...)} method that is very useful for generating
 * several {@link org.datavec.api.split.InputSplit}s from the main split.
 * <p>
 * This can be used for dividing your dataset into several subsets. For example, into training, validation and testing.
 * <p>
 * The {@link PathFilter} is useful for filtering the main split before generating the input splits array.
 * The second argument is a list of weights, which indicate a percentage of each input split.
 * <p>
 * The samples are divided in the following way -> totalSamples * (weight1/totalWeightSum, weight2/totalWeightSum, ...,
 * weightN/totalWeightSum)
 * <p>
 * {@link PathFilter} has two default implementations,
 * {@link org.datavec.api.io.filters.RandomPathFilter} that simple randomizes the order of paths in an array.
 * and
 * {@link org.datavec.api.io.filters.BalancedPathFilter} that randomizes the order of paths in an array and removes
 * paths randomly to have the same number of paths for each label. Further interlaces the paths on output based on
 * their labels, to obtain easily optimal batches for training.
 * <p>
 * Their usages are shown here.
 */
public class Ex05_SamplingBaseInputSplitExample {

    public static void main(String[] args) throws Exception {
        String dataLocalPath = "path/to/data";
        FileSplit fileSplit = new FileSplit(new File(dataLocalPath));

        String [] allowedExtensions =new String[]{"txt", "csv"};

        //Sampling with a RandomPathFilter
        PathFilter randomFilter = new RandomPathFilter(new Random(123), allowedExtensions);
        InputSplit[] inputSplits1 = fileSplit.sample(randomFilter, 10,10);

        System.out.println(String.format(("Random filtered splits -> Total(%d) = Splits of (%s)"), fileSplit.length(),
            String.join(" + ", () -> new InputSplitLengthIterator(inputSplits1))));

        //Sampling with a BalancedPathFilter
        PathFilter balancedFilter = new BalancedPathFilter(new Random(123), allowedExtensions, new ParentPathLabelGenerator());
        InputSplit[] inputSplits2 = fileSplit.sample(balancedFilter,10,10);

        System.out.println(String.format(("Balanced Splits are: %s"),
            String.join(" + ", () -> new InputSplitLengthIterator(inputSplits2))));
    }

    private static class InputSplitLengthIterator implements Iterator<CharSequence> {

        InputSplit[] inputSplits;
        int i;

        public InputSplitLengthIterator(InputSplit[] inputSplits) {
            this.inputSplits = inputSplits;
            this.i = 0;
        }

        @Override
        public boolean hasNext() {
            return i < inputSplits.length;
        }

        @Override
        public CharSequence next() {
            return String.valueOf(inputSplits[i++].length());
        }
    }
}
