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

import org.apache.hadoop.hive.ql.exec.Description
import org.apache.hadoop.hive.ql.exec.UDF

@Description(
    name = "sandbox_overloaded",
    value = "_FUNC_(x) - Takes a string or struct, returns downcased string or part of struct",
    extended = """
For example, you could do something like this:

  SELECT _FUNC_('Test Me');
    ==> test me

or this:

  SELECT _FUNC_(NAMED_STRUCT('value', 'Test Me', 'otherValue', 123));
    ==> {'value': 'test me', 'othervalue': 123}
"""
)
class SandboxOverloadedUDF : UDF() {

    class TestMe {
        var value:String? = null
        var otherValue:Int? = null

        fun undefined():Boolean {
            return value == null || otherValue == null
        }

        fun reset() {
            value = null
            otherValue = null
        }
    }

    val testMe = TestMe()

    fun evaluate(arg:TestMe?):TestMe {
        if(arg == null || arg.undefined()) {
            testMe.reset()
            return testMe
        }

        testMe.value = arg.value?.toLowerCase()
        testMe.otherValue = arg.otherValue

        return testMe
    }

    fun evaluate(input:String?):String? {
        return input?.toLowerCase()
    }

}
