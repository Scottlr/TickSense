package com.ticksense.storage;

import com.ticksense.common.TextValues;

final class StorageTexts
{
    private StorageTexts()
    {
    }

    static String requireText(String value, String fieldName)
    {
        return TextValues.requireText(value, fieldName);
    }
}
