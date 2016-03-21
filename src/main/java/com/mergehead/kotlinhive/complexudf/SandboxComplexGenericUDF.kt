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
import org.apache.hadoop.hive.ql.exec.UDFArgumentException
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory
import org.apache.hadoop.hive.serde2.objectinspector.SettableStructObjectInspector
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector

@Description(
    name = "sandbox_complex_generic",
    value = "_FUNC_(x) - Takes a struct and returns a downcased part of that struct",
    extended = """
For example, you could do something like this:

  SELECT _FUNC_(NAMED_STRUCT('value', 'Test Me', 'otherValue', 123));
    ==> {'value': 'test me', 'othervalue': 123}
"""
)
class SandboxComplexGenericUDF : GenericUDF() {

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

    val testObj = TestMe()

    var argOI:SettableStructObjectInspector? = null

    val outputOI = ObjectInspectorFactory.getReflectionObjectInspector(
        TestMe::class.java,
        ObjectInspectorFactory.ObjectInspectorOptions.JAVA
    )

    val converter by lazy { ObjectInspectorConverters.getConverter(argOI, outputOI) }

    override fun evaluate(args:Array<out DeferredObject>?):Any? {
        if(args?.size != 1) {
            testObj.reset()
            return testObj
        }

        val obj = converter!!.convert(args!!.first().get()) as TestMe
        if(obj.undefined()) {
            testObj.reset()
            return testObj
        }

        testObj.value = obj.value?.toLowerCase()
        testObj.otherValue = obj.otherValue
        return testObj
    }

    override fun initialize(args:Array<out ObjectInspector>?):ObjectInspector? {
        if(args?.size != 1 || args!![0] !is StructObjectInspector) {
            throw UDFArgumentException("SandboxComplexGenericUDF expects 1 struct argument")
        }
        argOI = args[0] as SettableStructObjectInspector
        return outputOI
    }

    override fun getDisplayString(args:Array<out String>):String? {
        return "_FUNC_(" + args[0]+ " )";
    }

}
