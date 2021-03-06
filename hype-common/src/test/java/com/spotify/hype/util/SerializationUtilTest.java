/*-
 * -\-\-
 * hype-run
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

package com.spotify.hype.util;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.function.Supplier;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class SerializationUtilTest {

  private Inner inner = new Inner();

  @Test
  public void roundtripLambda() throws Exception {
    Fn<String> fn = () -> inner.sup.get();
    Fn<String> fn1 = roundtrip(fn);

    inner.field = "something else";
    String result = fn1.run();

    assertEquals("hello", result);
  }

  @Test
  public void roundtripClosure() throws Exception {
    Fn<String> fn = closure("hello");
    Fn<String> fn1 = roundtrip(fn);

    String result = fn1.run();
    assertEquals("hello", result);
  }

  private Fn<String> roundtrip(Fn<String> fn) {
    Path path = SerializationUtil.serializeContinuation(fn);
    return (Fn<String>) SerializationUtil.readContinuation(path);
  }

  // this class is intentionally not implementing java.io.Serializable
  class Inner {
    String field = "hello";
    Supplier<String> sup = (Supplier & Serializable) () -> field;
  }

  private static Fn<String> closure(String arg) {
    return () -> arg;
  }
}
