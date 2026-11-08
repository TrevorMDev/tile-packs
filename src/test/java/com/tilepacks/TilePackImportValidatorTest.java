package com.tilepacks;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TilePackImportValidatorTest extends TilePacksTest {
    @Test
    public void validatesGroundMarkerCoordinatesOnlyAtImportBoundary() {
        assertTrue(TilePackImportValidator.isValid(
                "[{\"regionId\":65535,\"regionX\":63,\"regionY\":63,\"z\":3}]", gson));
        assertFalse(TilePackImportValidator.isValid("", gson));
        assertFalse(TilePackImportValidator.isValid("null", gson));
        assertFalse(TilePackImportValidator.isValid("[]", gson));
        assertFalse(TilePackImportValidator.isValid("[null]", gson));
        assertFalse(TilePackImportValidator.isValid("[{}]", gson));
        assertFalse(TilePackImportValidator.isValid(
                "[{\"regionId\":65536,\"regionX\":1,\"regionY\":1,\"z\":0}]", gson));
        assertFalse(TilePackImportValidator.isValid(
                "[{\"regionId\":1,\"regionX\":64,\"regionY\":1,\"z\":0}]", gson));
        assertFalse(TilePackImportValidator.isValid(
                "[{\"regionId\":1,\"regionX\":1,\"regionY\":1,\"z\":4}]", gson));
        assertFalse(TilePackImportValidator.isValid(
                "[{\"regionId\":1.5,\"regionX\":1,\"regionY\":1,\"z\":0}]", gson));
    }
}
