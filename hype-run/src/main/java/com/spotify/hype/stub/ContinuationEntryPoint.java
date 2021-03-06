/*-
 * -\-\-
 * hype-submitter
 * --
 * Copyright (C) 2016 - 2017 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.hype.stub;

import com.spotify.hype.util.Fn;
import com.spotify.hype.util.SerializationUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TODO: document.
 */
public class ContinuationEntryPoint {

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      throw new IllegalArgumentException("Usage: <staging-dir> <continuation-file> <output-file>");
    }

    final Path continuationPath = Paths.get(args[0], args[1]);
    if (!Files.exists(continuationPath)) {
      throw new IllegalArgumentException(continuationPath + " does not exist");
    }

    final Path returnValuePath = Paths.get(args[0], args[2]);
    if (Files.exists(returnValuePath)) {
      throw new IllegalArgumentException(returnValuePath + " already exists");
    }

    System.setProperty("user.dir", args[0]);
    final Fn<?> continuation = SerializationUtil.readContinuation(continuationPath);

    Object returnValue;
    try {
      returnValue = continuation.run();
    } catch (Throwable e) {
      e.printStackTrace();
      throw e;
    }

    SerializationUtil.serializeObject(returnValue, returnValuePath);
    System.out.println("returnValuePath = " + returnValuePath);
  }
}
