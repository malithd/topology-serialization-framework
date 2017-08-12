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
import org.locationtech.jts.geom.MultiPoint;
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

public class JTSModelMultipointTest {
    @Before
    public void setUp() throws Exception {
        Path filePath = Paths.get(Utils.TEST_FILE_LOCATION);
        Files.createFile(filePath);
    }

    @Test
    public void testSerializeGeoMultiPoint() throws Exception {
        GeometryFactory geometryFactory = new GeometryFactory();
        MultiPoint multiPoint = geometryFactory.createMultiPoint(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1)});
        System.out.println("-------------- Serializing JTS Model MultiPoint via Protobuf -------------------------");
        PBSerializationHandler pbSerializer = new PBSerializationHandler();
        FileOutputStream output = new FileOutputStream(Utils.TEST_FILE_LOCATION);
        try {
            pbSerializer.serialize(multiPoint, output);
        } finally {
            output.close();
        }
        assertTrue(new File(Utils.TEST_FILE_LOCATION).length() > 0);
        System.out.println("Successfully Serialized....");
    }

    @Test
    public void testDeserializeMultiGeoPoint() throws Exception {
        GeometryFactory geometryFactory = new GeometryFactory();
        MultiPoint multiPoint = geometryFactory.createMultiPoint(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1)});
        PBSerializationHandler pbSerializer = new PBSerializationHandler();
        FileOutputStream output = new FileOutputStream(Utils.TEST_FILE_LOCATION);
        try {
            pbSerializer.serialize(multiPoint, output);
            System.out.println("-------------- Deserializing JTS Model MultiPoint via Protobuf -------------------------");
            PBDeserializationHandler pbDeserializationHandler = new PBDeserializationHandler();
            MultiPoint multiPointDeserialized = (MultiPoint) pbDeserializationHandler.deserialize(new FileInputStream(Utils.TEST_FILE_LOCATION));
            assertEquals(multiPoint, multiPointDeserialized);
            System.out.println("Successfully Deserialized : " + multiPointDeserialized);
        } finally {
            output.close();
        }
    }

    @Test
    public void testSerializeGeoMultiPointWithAvro() throws Exception {
        GeometryFactory geometryFactory = new GeometryFactory();
        MultiPoint multiPoint = geometryFactory.createMultiPoint(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1)});
        System.out.println("-------------- Serializing JTS Model MultiPoint via Avro -------------------------");
        AvroSerializationHandler avroSerializer = new AvroSerializationHandler();
        FileOutputStream output = new FileOutputStream(Utils.TEST_FILE_LOCATION);
        try {
            avroSerializer.serialize(multiPoint, output);
        } finally {
            output.close();
        }
        assertTrue(new File(Utils.TEST_FILE_LOCATION).length() > 0);
        System.out.println("Successfully Serialized....");
    }

    @Test
    public void testDeserializeMultiGeoPointWithAvro() throws Exception {
        GeometryFactory geometryFactory = new GeometryFactory();
        MultiPoint multiPoint = geometryFactory.createMultiPoint(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1)});
        AvroSerializationHandler avroSerializer = new AvroSerializationHandler();
        FileOutputStream output = new FileOutputStream(Utils.TEST_FILE_LOCATION);
        try {
            avroSerializer.serialize(multiPoint, output);
            System.out.println("-------------- Deserializing JTS Model MultiPoint via Avro -------------------------");
            AvroDeserializationHandler avroDeserializationHandler = new AvroDeserializationHandler();
            MultiPoint multiPointDeserialized = (MultiPoint) avroDeserializationHandler.deserialize(new FileInputStream(Utils.TEST_FILE_LOCATION));
            assertEquals(multiPoint, multiPointDeserialized);
            System.out.println("Successfully Deserialized : " + multiPointDeserialized);
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