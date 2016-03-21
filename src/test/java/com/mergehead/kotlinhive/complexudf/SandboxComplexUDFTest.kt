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

class SandboxComplexUDFTest : TestBase("sandbox_complex", SandboxComplexUDF::class){

    @Suppress("unused")
    @field:HiveSQL(files = arrayOf())
    var hiveShell:HiveShell? = null

    /**
     * Note that hive will downcase all field names when serializing, so these might not match all your camelcase names
     * for the actual UDF members.
     */
    data class TestMe(val value:String?, val othervalue:Int?)

    fun queryForClass(queryStr:String):TestMe? {
        return queryForJSON(queryStr)
    }

    @Test
    fun basicStructInput() {
        assertEquals(
            TestMe("test me", 123),
            queryForClass("""
                SELECT sandbox_complex(
                    NAMED_STRUCT(
                        'value', 'Test Me',
                        'otherValue', 123
                    )
                )
            """)
        )
    }

    @Test
    fun nullValue() {
        assertEquals(
            TestMe(null, null),
            queryForClass("""
                SELECT sandbox_complex(
                    NAMED_STRUCT(
                        'value', STRING(NULL),
                        'otherValue', 123
                    )
                )
            """)
        )
    }

    @Test
    fun nullOtherValue() {
        assertEquals(
            TestMe(null, null),
            queryForClass("""
                SELECT sandbox_complex(
                    NAMED_STRUCT(
                        'value', "Test Me",
                        'otherValue', INT(NULL)
                    )
                )
            """)
        )
    }

    @Test
    fun nullBoth() {
        assertEquals(
            TestMe(null, null),
            queryForClass("""
                SELECT sandbox_complex(
                    NAMED_STRUCT(
                        'value', STRING(NULL),
                        'otherValue', INT(NULL)
                    )
                )
            """)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun firstMemberWrongType() {
        queryForClass("""
            SELECT sandbox_complex(
                NAMED_STRUCT(
                    'value', 123,
                    'otherValue', 123
                )
            )
        """)
    }

    @Test(expected = IllegalArgumentException::class)
    fun secondMemberWrongType() {
        queryForClass("""
            SELECT sandbox_complex(
                NAMED_STRUCT(
                    'value', 'Hola',
                    'otherValue', 'banana'
                )
            )
        """)
    }

}
