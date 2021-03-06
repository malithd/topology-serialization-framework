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

package org.n52.tsf.model.vector.jts.locationtech;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.*;
import org.n52.tsf.model.DeserializationHandler;
import org.n52.tsf.model.DeserializerType;
import org.n52.tsf.serialization.protobuf.gen.GeoProtobuf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provides the JTS to ProtoBuf Deserialization functionality.
 */
public class PBDeserializationHandlerLT extends DeserializationHandler {
    private final static Logger logger = Logger.getLogger(PBDeserializationHandlerLT.class);
    private GeometryFactory geometryFactory;
    private InputStream inputStream;


    public PBDeserializationHandlerLT(InputStream inputStream) {
        super(DeserializerType.PROTOBUF_DESERIALIZER_VS);
        this.geometryFactory = new GeometryFactory();
        this.inputStream = inputStream;

    }

    public Object deserialize() throws IOException {
        GeoProtobuf.Geometry pbGeometry = GeoProtobuf.Geometry.parseDelimitedFrom(inputStream);
        Object jtsGeometry = null;
        switch (pbGeometry.getType()) {
            case POINT:
                jtsGeometry = deserializePoint(pbGeometry);
                break;
            case LINESTRING:
                jtsGeometry = deserializeLineString(pbGeometry);
                break;
            case POLYGON:
                jtsGeometry = deserializePolygon(pbGeometry);
                break;
            case MULTIPOINT:
                jtsGeometry = deserializeMultiPoint(pbGeometry);
                break;
            case MULTILINESTRING:
                jtsGeometry = deserializeMultiLineString(pbGeometry);
                break;
            case LINEARRING:
                jtsGeometry = deserializeLinearRing(pbGeometry);
                break;
            case MULTIPOLYGON:
                jtsGeometry = deserializeMultiPolygon(pbGeometry);
                break;
            case GEOMETRYCOLLECTION:
                jtsGeometry = deserializeGeoCollection(pbGeometry);
                break;
            case TRIANGLE:
                jtsGeometry = deserializeTriangle(pbGeometry);
                break;
            case LINE:
                jtsGeometry = deserializeLine(pbGeometry);
                break;
            default:
                logger.error("Unsupported Geometric type for Protobuf deserialization");
        }
        return jtsGeometry;
    }

    public void close() throws IOException {
        inputStream.close();
    }

    private Point deserializePoint(GeoProtobuf.Geometry pbGeometry) {
        Coordinate jtsCoordinate = createJtsCoordinate(pbGeometry.getCoordinates(0));
        Point point = geometryFactory.createPoint(jtsCoordinate);
        return point;
    }

    private MultiPoint deserializeMultiPoint(GeoProtobuf.Geometry pbGeometry) {
        Point[] jtsPoints = pbGeometry.getGeometriesList().
                stream().map(this::deserializePoint).collect(Collectors.toList()).stream().toArray(Point[]::new);
        MultiPoint multiPoint = geometryFactory.createMultiPoint(jtsPoints);
        return multiPoint;
    }

    private LineString deserializeLineString(GeoProtobuf.Geometry pbGeometry) {
        Coordinate[] jtsCoordinates = pbGeometry.getCoordinatesList().
                stream().map(this::createJtsCoordinate).collect(Collectors.toList()).stream().toArray(Coordinate[]::new);
        LineString lineString = geometryFactory.createLineString(jtsCoordinates);
        return lineString;
    }

    public LineSegment deserializeLine(GeoProtobuf.Geometry pbGeometry) throws IOException {
        Coordinate[] jtsCoordinates = pbGeometry.getCoordinatesList().
                stream().map(this::createJtsCoordinate).collect(Collectors.toList()).stream().toArray(Coordinate[]::new);
        LineSegment lineSegment = new LineSegment(jtsCoordinates[0], jtsCoordinates[1]);
        return lineSegment;
    }

    public Triangle deserializeTriangle(GeoProtobuf.Geometry pbGeometry) throws IOException {
        Coordinate[] jtsCoordinates = pbGeometry.getCoordinatesList().
                stream().map(this::createJtsCoordinate).collect(Collectors.toList()).stream().toArray(Coordinate[]::new);
        Triangle triangle = new Triangle(jtsCoordinates[0], jtsCoordinates[1], jtsCoordinates[2]);
        return triangle;
    }

    private LinearRing deserializeLinearRing(GeoProtobuf.Geometry pbGeometry) {
        Coordinate[] jtsCoordinates = pbGeometry.getCoordinatesList().
                stream().map(this::createJtsCoordinate).collect(Collectors.toList()).stream().toArray(Coordinate[]::new);
        LinearRing linearRing = geometryFactory.createLinearRing(jtsCoordinates);
        return linearRing;
    }

    private MultiLineString deserializeMultiLineString(GeoProtobuf.Geometry pbGeometry) {
        LineString[] jtsLineStrings = pbGeometry.getGeometriesList().
                stream().map(this::deserializeLineString).collect(Collectors.toList()).stream().toArray(LineString[]::new);
        MultiLineString multiLineString = geometryFactory.createMultiLineString(jtsLineStrings);
        return multiLineString;
    }

    private Polygon deserializePolygon(GeoProtobuf.Geometry pbGeometry) {
        Polygon polygon;
        if (pbGeometry.getGeometriesCount() == 1) {
            GeoProtobuf.Geometry exteriorLS = pbGeometry.getGeometries(0);
            Coordinate[] jtsCoordinates = exteriorLS.getCoordinatesList().
                    stream().map(this::createJtsCoordinate).collect(Collectors.toList()).stream().toArray(Coordinate[]::new);
            polygon = geometryFactory.createPolygon(jtsCoordinates);

        } else {
            GeoProtobuf.Geometry exteriorLS = pbGeometry.getGeometries(0);
            List<GeoProtobuf.Geometry> interiorLSs = pbGeometry.getGeometries(1).getGeometriesList();

            Coordinate[] exCoordinates = exteriorLS.getCoordinatesList().
                    stream().map(this::createJtsCoordinate).collect(Collectors.toList()).stream().toArray(Coordinate[]::new);

            LinearRing exteriorLR = geometryFactory.createLinearRing(exCoordinates);

            List<LinearRing> jtsInteriorLRs = new ArrayList();
            for (GeoProtobuf.Geometry geo : interiorLSs) {
                Coordinate[] inCoordinates = geo.getCoordinatesList().
                        stream().map(this::createJtsCoordinate).collect(Collectors.toList()).stream().toArray(Coordinate[]::new);
                jtsInteriorLRs.add(geometryFactory.createLinearRing(inCoordinates));
            }
            polygon = geometryFactory.createPolygon(exteriorLR, jtsInteriorLRs.stream().toArray(LinearRing[]::new));
        }
        return polygon;
    }

    private MultiPolygon deserializeMultiPolygon(GeoProtobuf.Geometry pbGeometry) {
        Polygon[] jtsPolygons = pbGeometry.getGeometriesList().
                stream().map(this::deserializePolygon).collect(Collectors.toList()).stream().toArray(Polygon[]::new);
        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(jtsPolygons);
        return multiPolygon;
    }

    private Coordinate createJtsCoordinate(GeoProtobuf.Coordinate pbCoordinate) {
        Coordinate jtsCoordinate = new Coordinate(pbCoordinate.getX(), pbCoordinate.getY());
        return jtsCoordinate;
    }

    private GeometryCollection deserializeGeoCollection(GeoProtobuf.Geometry pbGeometry) {
        Geometry[] jtsGeometries = pbGeometry.getGeometriesList().
                stream().map(this::deserializeGeometry).collect(Collectors.toList()).stream().toArray(Geometry[]::new);
        return geometryFactory.createGeometryCollection(jtsGeometries);
    }

    private Geometry deserializeGeometry(GeoProtobuf.Geometry pbGeometry) {
        switch (pbGeometry.getType()) {
            case POINT:
                return deserializePoint(pbGeometry);
            case LINESTRING:
                return deserializeLineString(pbGeometry);
            case POLYGON:
                return deserializePolygon(pbGeometry);
            case MULTIPOINT:
                return deserializeMultiPoint(pbGeometry);
            case MULTILINESTRING:
                return deserializeMultiLineString(pbGeometry);
            case LINEARRING:
                return deserializeLinearRing(pbGeometry);
            case MULTIPOLYGON:
                return deserializeMultiPolygon(pbGeometry);
            default:
                logger.error("Unsupported Geometric type for Protobuf deserialization");
                return null;
        }
    }
}
