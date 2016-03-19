/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 mergehead.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mergehead.kotlinhive.complexudf

import com.klarna.hiverunner.HiveShell
import com.klarna.hiverunner.annotations.HiveSQL
import org.junit.Assert.assertEquals
import org.junit.Test

class SandboxCollectionsUDFTest : TestBase("sandbox_collections", SandboxCollectionsUDF::class){

    @Suppress("unused")
    @field:HiveSQL(files = arrayOf())
    var hiveShell:HiveShell? = null

    override fun setupHQL() {
        execute("""
            CREATE TABLE special_values (
                empty_array ARRAY<STRUCT<
                    value:STRING,
                    otherValue:INT
                >>,
                nulled_array ARRAY<STRUCT<
                    value:STRING,
                    otherValue:INT
                >>,
                empty_map MAP<STRING, STRUCT<
                    value:STRING,
                    otherValue:INT
                >>,
                nulled_map MAP<STRING, STRUCT<
                    value:STRING,
                    otherValue:INT
                >>
            )
        """)
        childHiveShell.insertInto("default", "special_values").addRow(
            emptyArray<Any>(),
            null,
            emptyMap<Any, Any>(),
            null
        ).commit()
    }

    /**
     * Note that hive will downcase all field names, so these might not match all your camelcase names for the
     * actual UDF members.
     */
    data class TestMe(val value:String?, val othervalue:Int?)

    fun queryForList(queryStr:String):List<TestMe?>? {
        return queryForJSON(queryStr)
    }

    fun queryForMap(queryStr:String):Map<String?, TestMe?>? {
        return queryForJSON(queryStr)
    }

    //////////// SINGLE CASE

    @Test
    fun basicStructInputStruct() {
        assertEquals(
            listOf(TestMe("test me", 123)),
            queryForList("""
                SELECT sandbox_collections(
                    NAMED_STRUCT(
                        'value', 'Test Me',
                        'otherValue', 123
                    )
                )
            """)
        )
    }

    @Test
    fun nullValueStruct() {
        assertEquals(
            listOf(TestMe(null, null)),
            queryForList("""
                SELECT sandbox_collections(
                    NAMED_STRUCT(
                        'value', STRING(NULL),
                        'otherValue', 123
                    )
                )
            """)
        )
    }

    @Test
    fun nullOtherValueStruct() {
        assertEquals(
            listOf(TestMe(null, null)),
            queryForList("""
                SELECT sandbox_collections(
                    NAMED_STRUCT(
                        'value', "Test Me",
                        'otherValue', INT(NULL)
                    )
                )
            """)
        )
    }

    @Test
    fun nullBothStruct() {
        assertEquals(
            listOf(TestMe(null, null)),
            queryForList("""
                SELECT sandbox_collections(
                    NAMED_STRUCT(
                        'value', STRING(NULL),
                        'otherValue', INT(NULL)
                    )
                )
            """)
        )
    }

    //////////// ARRAY CASE

    @Test
    fun nullInputList() {
        assertEquals(
            "NULL",
            queryOne("SELECT sandbox_collections(nulled_array) FROM special_values")
        )
    }

    @Test
    fun emptyInputList() {
        assertEquals(
            "[]",
            queryOne("SELECT sandbox_collections(empty_array) FROM special_values")
        )
    }

    @Test
    fun basicStructInputList() {
        assertEquals(
            listOf(TestMe("test me", 123), TestMe("test me too", 456)),
            queryForList("""
                SELECT sandbox_collections(ARRAY(
                    NAMED_STRUCT(
                        'value', 'Test Me',
                        'otherValue', 123
                    ),
                    NAMED_STRUCT(
                        'value', 'Test Me Too',
                        'otherValue', 456
                    )
                ))
            """)
        )
    }

    @Test
    fun nullValueList() {
        assertEquals(
            listOf(TestMe(null, null), TestMe("test me too", 456)),
            queryForList("""
                SELECT sandbox_collections(ARRAY(
                    NAMED_STRUCT(
                        'value', STRING(NULL),
                        'otherValue', 123
                    ),
                    NAMED_STRUCT(
                        'value', 'Test Me Too',
                        'otherValue', 456
                    )
                ))
            """)
        )
    }

    @Test
    fun nullOtherValueList() {
        assertEquals(
            listOf(TestMe(null, null), TestMe("test me too", 456)),
            queryForList("""
                SELECT sandbox_collections(ARRAY(
                    NAMED_STRUCT(
                        'value', "Test Me",
                        'otherValue', INT(NULL)
                    ),
                    NAMED_STRUCT(
                        'value', 'Test Me Too',
                        'otherValue', 456
                    )
                ))
            """)
        )
    }

    @Test
    fun nullBothList() {
        assertEquals(
            listOf(TestMe(null, null), TestMe("test me too", 456)),
            queryForList("""
                SELECT sandbox_collections(ARRAY(
                    NAMED_STRUCT(
                        'value', STRING(NULL),
                        'otherValue', INT(NULL)
                    ),
                    NAMED_STRUCT(
                        'value', 'Test Me Too',
                        'otherValue', 456
                    )
                ))
            """)
        )
    }

    @Test
    fun nullStructList() {
        assertEquals(
            listOf(TestMe(null, null), TestMe("test me too", 456)),
            queryForList("""
                SELECT sandbox_collections(ARRAY(
                    NULL,
                    NAMED_STRUCT(
                        'value', 'Test Me Too',
                        'otherValue', 456
                    )
                ))
            """)
        )
    }

    //////////// MAP CASE

    @Test
    fun nullInputMap() {
        assertEquals(
            "NULL",
            queryOne("SELECT sandbox_collections(nulled_map) FROM special_values")
        )
    }

    @Test
    fun emptyInputMap() {
        assertEquals(
            "{}",
            queryOne("SELECT sandbox_collections(empty_map) FROM special_values")
        )
    }

    @Test
    fun basicStructInputMap() {
        assertEquals(
            mapOf(
                "one" to TestMe("test me", 123),
                "two" to TestMe("test me too", 456)
            ),
            queryForMap("""
                SELECT sandbox_collections(MAP(
                    'one', NAMED_STRUCT(
                        'value', 'Test Me',
                        'otherValue', 123
                    ),
                    'two', NAMED_STRUCT(
                        'value', 'Test Me Too',
                        'otherValue', 456
                    )
                ))
            """)
        )
    }

    @Test
    fun nullValueMap() {
        assertEquals(
            mapOf(
                "one" to TestMe(null, null),
                "two" to TestMe("test me too", 456)
            ),
            queryForMap("""
                SELECT sandbox_collections(MAP(
                    'one', NAMED_STRUCT(
                        'value', STRING(NULL),
                        'otherValue', 123
                    ),
                    'two', NAMED_STRUCT(
                        'value', 'Test Me Too',
                        'otherValue', 456
                    )
                ))
            """)
        )
    }

    @Test
    fun nullOtherValueMap() {
        assertEquals(
            mapOf(
                "one" to TestMe(null, null),
                "two" to TestMe("test me too", 456)
            ),
            queryForMap("""
                SELECT sandbox_collections(MAP(
                    'one', NAMED_STRUCT(
                        'value', "Test Me",
                        'otherValue', INT(NULL)
                    ),
                    'two', NAMED_STRUCT(
                        'value', 'Test Me Too',
                        'otherValue', 456
                    )
                ))
            """)
        )
    }

    @Test
    fun nullBothMap() {
        assertEquals(
            mapOf(
                "one" to TestMe(null, null),
                "two" to TestMe("test me too", 456)
            ),
            queryForMap("""
                SELECT sandbox_collections(MAP(
                    'one', NAMED_STRUCT(
                        'value', STRING(NULL),
                        'otherValue', INT(NULL)
                    ),
                    'two', NAMED_STRUCT(
                        'value', 'Test Me Too',
                        'otherValue', 456
                    )
                ))
            """)
        )
    }

    @Test
    fun nullStructMap() {
        assertEquals(
            mapOf(
                "one" to TestMe(null, null),
                "two" to TestMe("test me too", 456)
            ),
            queryForMap("""
                SELECT sandbox_collections(MAP(
                    'one', NULL,
                    'two', NAMED_STRUCT(
                        'value', 'Test Me Too',
                        'otherValue', 456
                    )
                ))
            """)
        )
    }

    //////////// VAR-ARG CASE

    @Test
    fun basicStructInputVarArg() {
        assertEquals(
            listOf(
                TestMe("test me", 123),
                TestMe("test me too", 456),
                TestMe("test me also", 789)
            ),
            queryForList("""
                SELECT sandbox_collections(
                    NAMED_STRUCT(
                        'value', 'Test Me',
                        'otherValue', 123
                    ),
                    NAMED_STRUCT(
                        'value', 'Test Me Too',
                        'otherValue', 456
                    ),
                    NAMED_STRUCT(
                        'value', 'Test Me Also',
                        'otherValue', 789
                    )
                )
            """)
        )
    }

    @Test
    fun nullValueVarArg() {
        assertEquals(
            listOf(
                TestMe("test me", 123),
                TestMe(null, null),
                TestMe("test me also", 789)
            ),
            queryForList("""
                SELECT sandbox_collections(
                    NAMED_STRUCT(
                        'value', 'Test Me',
                        'otherValue', 123
                    ),
                    NAMED_STRUCT(
                        'value', STRING(NULL),
                        'otherValue', 456
                    ),
                    NAMED_STRUCT(
                        'value', 'Test Me Also',
                        'otherValue', 789
                    )
                )
            """)
        )
    }

    @Test
    fun nullOtherValueVarArg() {
        assertEquals(
            listOf(
                TestMe("test me", 123),
                TestMe(null, null),
                TestMe("test me also", 789)
            ),
            queryForList("""
                SELECT sandbox_collections(
                    NAMED_STRUCT(
                        'value', 'Test Me',
                        'otherValue', 123
                    ),
                    NAMED_STRUCT(
                        'value', 'Test Me Too',
                        'otherValue', INT(NULL)
                    ),
                    NAMED_STRUCT(
                        'value', 'Test Me Also',
                        'otherValue', 789
                    )
                )
            """)
        )
    }

    @Test
    fun nullBothVarArg() {
        assertEquals(
            listOf(
                TestMe("test me", 123),
                TestMe(null, null),
                TestMe("test me also", 789)
            ),
            queryForList("""
                SELECT sandbox_collections(
                    NAMED_STRUCT(
                        'value', 'Test Me',
                        'otherValue', 123
                    ),
                    NAMED_STRUCT(
                        'value', STRING(NULL),
                        'otherValue', INT(NULL)
                    ),
                    NAMED_STRUCT(
                        'value', 'Test Me Also',
                        'otherValue', 789
                    )
                )
            """)
        )
    }

    @Test
    fun nullStructValueVarArg() {
        assertEquals(
            listOf(
                TestMe("test me", 123),
                TestMe(null, null),
                TestMe("test me also", 789)
            ),
            queryForList("""
                SELECT sandbox_collections(
                    NAMED_STRUCT(
                        'value', 'Test Me',
                        'otherValue', 123
                    ),
                    NULL,
                    NAMED_STRUCT(
                        'value', 'Test Me Also',
                        'otherValue', 789
                    )
                )
            """)
        )
    }

}
