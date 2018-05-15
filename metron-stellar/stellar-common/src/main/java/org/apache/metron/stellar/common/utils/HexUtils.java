/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.metron.stellar.common.utils;

public enum HexUtils {
  INSTANCE;

  final protected static char[] encoding = "0123456789ABCDEF".toCharArray();

  /**
   * Integer array specific implementation inspired by  https://stackoverflow.com/a/28611711
   * @param arr
   * @return
   */
  public String toHexString(int[] arr, int len) {
    char[] encodedChars = new char[len * 4 * 2];
    for (int i = 0; i < len; i++) {
      int v = arr[i];
      int idx = i * 4 * 2;
      for (int j = 0; j < 8; j++) {
        encodedChars[idx + j] = encoding[(v >>> ((7-j)*4)) & 0x0F];
      }
    }
    return new String(encodedChars);
  }

  public String toHexString(int[] arr) {
    return toHexString(arr, arr.length);

  }
}
