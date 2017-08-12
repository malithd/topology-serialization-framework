//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

package org.n52.tsf.model.vector.jts.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.n52.tsf.model.vector.jts.AvroDeserializationHandler;
import org.n52.tsf.model.vector.jts.AvroSerializationHandler;
import org.n52.tsf.model.vector.jts.PBDeserializationHandler;
import org.n52.tsf.model.vector.jts.PBSerializationHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JTSModelLinearRingTest {

    @Before
    public void setUp() throws Exception {
        Path filePath = Paths.get(Utils.TEST_FILE_LOCATION);
        Files.createFile(filePath);
    }

    @Test
    public void testSerializeGeoLinearRing() throws Exception {
        GeometryFactory geometryFactory = new GeometryFactory();
        LinearRing linearRing = geometryFactory.createLinearRing(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, 0)});
        System.out.println("-------------- Serializing JTS Model LinearRing via Protobuf -------------------------");
        PBSerializationHandler pbSerializer = new PBSerializationHandler();
        FileOutputStream output = new FileOutputStream(Utils.TEST_FILE_LOCATION);
        try {
            pbSerializer.serialize(linearRing, output);
        } finally {
            output.close();
        }
        assertTrue(new File(Utils.TEST_FILE_LOCATION).length() > 0);
        System.out.println("Successfully Serialized....");
    }

    @Test
    public void testDeserializeGeoLinearRing() throws Exception {
        GeometryFactory geometryFactory = new GeometryFactory();
        LinearRing linearRing = geometryFactory.createLinearRing(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, 0)});
        PBSerializationHandler pbSerializer = new PBSerializationHandler();
        FileOutputStream output = new FileOutputStream(Utils.TEST_FILE_LOCATION);
        try {
            pbSerializer.serialize(linearRing, output);
            System.out.println("-------------- Deserializing JTS Model LinearRing via Protobuf -------------------------");
            PBDeserializationHandler pbDeserializationHandler = new PBDeserializationHandler();
            LinearRing linearRingDeserialized = (LinearRing) pbDeserializationHandler.deserialize(new FileInputStream(Utils.TEST_FILE_LOCATION));
            assertEquals(linearRing, linearRingDeserialized);
            System.out.println("Successfully Deserialized : " + linearRingDeserialized);
        } finally {
            output.close();
        }
    }

    @Test
    public void testSerializeGeoLinearRingWithAvro() throws Exception {
        GeometryFactory geometryFactory = new GeometryFactory();
        LinearRing linearRing = geometryFactory.createLinearRing(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, 0)});
        System.out.println("-------------- Serializing JTS Model LinearRing via Avro -------------------------");
        AvroSerializationHandler avroSerializer = new AvroSerializationHandler();
        FileOutputStream output = new FileOutputStream(Utils.TEST_FILE_LOCATION);
        try {
            avroSerializer.serialize(linearRing, output);
        } finally {
            output.close();
        }
        assertTrue(new File(Utils.TEST_FILE_LOCATION).length() > 0);
        System.out.println("Successfully Serialized....");
    }

    @Test
    public void testDeserializeGeoLinearRingWithAvro() throws Exception {
        GeometryFactory geometryFactory = new GeometryFactory();
        LinearRing linearRing = geometryFactory.createLinearRing(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, 0)});
        AvroSerializationHandler avroSerializer = new AvroSerializationHandler();
        FileOutputStream output = new FileOutputStream(Utils.TEST_FILE_LOCATION);
        try {
            avroSerializer.serialize(linearRing, output);
            System.out.println("-------------- Deserializing JTS Model LinearRing via Avro -------------------------");
            AvroDeserializationHandler avroDeserializationHandler = new AvroDeserializationHandler();
            LinearRing linearRingDeserialized = (LinearRing) avroDeserializationHandler.deserialize(new FileInputStream(Utils.TEST_FILE_LOCATION));
            assertEquals(linearRing, linearRingDeserialized);
            System.out.println("Successfully Deserialized : " + linearRingDeserialized);
        } finally {
            output.close();
        }
    }

    @After
    public void tearDown() throws Exception {
        Path filePath = Paths.get(Utils.TEST_FILE_LOCATION);
        Files.deleteIfExists(filePath);
    }
}