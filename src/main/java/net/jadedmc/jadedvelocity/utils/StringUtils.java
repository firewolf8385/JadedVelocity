/*
 * This file is part of JadedVelocity, licensed under the MIT License.
 *
 *  Copyright (c) JadedMC
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package net.jadedmc.jadedvelocity.utils;

import java.util.Arrays;
import java.util.List;

/**
 * A collection of String-related utilities.
 */
public class StringUtils {

    /**
     * Join multiple strings together into one string.
     * @param args List of strings to join together.
     * @param separator What should be between each string.
     * @return Combined string.
     */
    public static String join(List<String> args, String separator) {
        StringBuilder temp = new StringBuilder();

        for(String str : args) {
            if(!temp.toString().equals("")) {
                temp.append(separator);
            }

            temp.append(str);
        }

        return temp.toString();
    }

    /**
     * Join multiple strings together into one string.
     * @param args Array of strings to join together.
     * @param separator What should be between each string.
     * @return Combined string.
     */
    public static String join(String[] args, String separator) {
        return join(Arrays.asList(args), separator);
    }
}