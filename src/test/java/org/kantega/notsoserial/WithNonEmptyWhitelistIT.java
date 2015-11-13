/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.notsoserial;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import org.junit.Test;

import javax.xml.transform.TransformerConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.kantega.notsoserial.WithAgentIT.attachAgent;

/**
 *
 */
public class WithNonEmptyWhitelistIT {




    @Test
    public void javaWhiteListShouldPreventAttachYetAllowArrayList() throws TransformerConfigurationException, IOException, ClassNotFoundException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {

        System.setProperty("notsoserial.whitelist", "src/test/resources/whitelist-java.txt");

        attachAgent();

        byte[] ser = Files.readAllBytes(Paths.get("target").resolve("bytes.ser"));

        try {
            System.setProperty("pwned", "false");
            // Deserializing should not flip pwned to true
            deserialize(ser);
        } catch (ClassCastException e) {
            // Ignore, happens after exploit effect

        } catch (UnsupportedOperationException e) {
            // The object should not be deserializable
        }
        assertThat(System.getProperty("pwned"), is("false"));

        Queue<String> strings = (Queue<String>) deserialize(serialize(new PriorityQueue<String>(Arrays.asList("one", "two", "three"))));
    }

    private byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(object);
        return bout.toByteArray();
    }


    private Object deserialize(byte[] ser) throws IOException, ClassNotFoundException {
        ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(ser));
        return stream.readObject();
    }
}
