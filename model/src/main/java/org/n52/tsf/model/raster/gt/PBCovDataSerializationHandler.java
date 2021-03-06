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

package org.n52.tsf.model.raster.gt;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.imageio.geotiff.GeoTiffIIOMetadataDecoder;
import org.geotools.coverage.grid.io.imageio.geotiff.TiePoint;
import org.geotools.data.WorldFileReader;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.referencing.CRS;
import org.n52.tsf.serialization.protobuf.gen.GeoProtobufCov;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static java.lang.Double.NaN;

/**
 * This class provides the Geotools coverage data to ProtoBuf Serialization functionality.
 */
public class PBCovDataSerializationHandler {

    public void serialize(File geotifFile, OutputStream outputStream, boolean serializeMetaData) throws Exception {
        GeoProtobufCov.Grid.Builder gridBuilder = GeoProtobufCov.Grid.newBuilder();
        if (serializeMetaData) {
            setMetaData(gridBuilder, geotifFile, null);
        } else {
            transformGridToWorld(gridBuilder, geotifFile);
        }
        gridBuilder.build().writeTo(outputStream);
    }

    public void serialize(File geotifFile, File tfwfile, OutputStream outputStream) throws Exception {
        GeoProtobufCov.Grid.Builder gridBuilder = GeoProtobufCov.Grid.newBuilder();
        setMetaData(gridBuilder, geotifFile, tfwfile);
        gridBuilder.build().writeTo(outputStream);
    }


    public void setMetaData(GeoProtobufCov.Grid.Builder gridBuilder, File geotifFile, File tfwFile) throws IOException, FactoryException {
        GeoTiffReader fileReader = new GeoTiffReader(geotifFile);
        GeoTiffIIOMetadataDecoder metadata = fileReader.getMetadata();
        AbstractGridFormat format = GridFormatFinder.findFormat(geotifFile);
        GridCoverage2DReader reader = format.getReader(geotifFile);
        GridEnvelope dimensions = reader.getOriginalGridRange();
        GridCoordinates maxDimensions = dimensions.getHigh();
        GridCoverage2D coverage = reader.read(null);
        gridBuilder.setMaxWidth(maxDimensions.getCoordinateValue(0) + 1);
        gridBuilder.setMaxHight(maxDimensions.getCoordinateValue(1) + 1);
        gridBuilder.setSourceCrs(CRS.lookupIdentifier(coverage.getCoordinateReferenceSystem(), true));
        gridBuilder.setColorSpace(coverage.getRenderedImage().getColorModel().getColorSpace().getType());

        if (metadata.hasTiePoints()) {
            for (TiePoint tp : metadata.getModelTiePoints()) {
                GeoProtobufCov.TiePoint.Builder tiePoint = GeoProtobufCov.TiePoint.newBuilder();
                for (double value : tp.getData()) {
                    tiePoint.addValue(value);
                }
                gridBuilder.addTiePoints(tiePoint.build());
            }
        }

        if (metadata.hasPixelScales()) {
            GeoProtobufCov.PixelScale.Builder pixelScale = GeoProtobufCov.PixelScale.newBuilder();
            pixelScale.setScaleX(metadata.getModelPixelScales().getScaleX());
            pixelScale.setScaleY(metadata.getModelPixelScales().getScaleY());
            pixelScale.setScaleZ(metadata.getModelPixelScales().getScaleZ());
            gridBuilder.setPixelScale(pixelScale.build());
        }
        setTFWData(gridBuilder, tfwFile);
    }

    private void transformGridToWorld(GeoProtobufCov.Grid.Builder gridBuilder, File geotifFile) throws IOException, TransformException {
        AbstractGridFormat format = GridFormatFinder.findFormat(geotifFile);
        GridCoverage2DReader reader = format.getReader(geotifFile);
        GridEnvelope dimensions = reader.getOriginalGridRange();
        GridCoordinates maxDimensions = dimensions.getHigh();
        int w = maxDimensions.getCoordinateValue(0) + 1;
        int h = maxDimensions.getCoordinateValue(1) + 1;
        GridCoverage2D coverage = reader.read(null);
        GridGeometry2D geometry = coverage.getGridGeometry();

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {

                org.geotools.geometry.Envelope2D pixelEnvelop =
                        geometry.gridToWorld(new GridEnvelope2D(i, j, 1, 1));

                double latitude = pixelEnvelop.getCenterX();
                double longitude = pixelEnvelop.getCenterY();

                GeoProtobufCov.GridPoint.Builder gridPoint = GeoProtobufCov.GridPoint.newBuilder();
                gridPoint.setLatitude(latitude);
                gridPoint.setLongitude(longitude);
                gridBuilder.addGridPoints(gridPoint.build());
            }
        }
        setTFWData(gridBuilder, null);
    }

    private void setTFWData(GeoProtobufCov.Grid.Builder avroBuilder, File tfwFile) throws IOException {
        if (tfwFile != null) {
            WorldFileReader worldFileReader = new WorldFileReader(tfwFile);
            avroBuilder.setXulc(worldFileReader.getXULC());
            avroBuilder.setYulc(worldFileReader.getYULC());
            avroBuilder.setXPixelSize(worldFileReader.getXPixelSize());
            avroBuilder.setYPixelSize(worldFileReader.getYPixelSize());
            avroBuilder.setXRotation(worldFileReader.getRotationX());
            avroBuilder.setYRotation(worldFileReader.getRotationY());
        } else {
            avroBuilder.setXulc(NaN);
            avroBuilder.setYulc(NaN);
            avroBuilder.setXPixelSize(NaN);
            avroBuilder.setYPixelSize(NaN);
            avroBuilder.setXRotation(NaN);
            avroBuilder.setYRotation(NaN);
        }
    }
}
