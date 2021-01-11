package dev.minutest.experimental


// Experiments with the typesystem outside the current types

private class NewNode<in F>(
    val annotations: List<NewAnnotation<F>>
)

private class NewNodeBuilder<F> {

    private val annotations: MutableList<NewAnnotation<F>> = mutableListOf()

    fun addAnnotation(a: NewAnnotation<F>) {
        annotations.add(a)
    }

    fun build(): NewNode<F> {
        val base = NewNode(annotations)
        return if (annotations.isEmpty())
            base
        else
            annotations.first().transform(base)
    }
}

private interface NewNodeTransform<F> {
    fun transform(node: NewNode<F>): NewNode<F>
}

private interface NewAnnotation<in F> {
    fun <F2: F> transform(): NewNodeTransform<F2>
    fun <F2: F> transform(node: NewNode<F2>): NewNode<F2>
}

operator fun <F, A: NewAnnotation<F>> A.plus(other: A): List<A> = listOf(this, other)

private class PlainAnnotation<in F> : NewAnnotation<F> {
    override fun <F2 : F> transform(): NewNodeTransform<F2> = object: NewNodeTransform<F2> {
        override fun transform(node: NewNode<F2>): NewNode<F2> = node
    }
    override fun <F2 : F> transform(node: NewNode<F2>): NewNode<F2> = node
}

@Suppress("UNUSED_VARIABLE")
private fun scope() {

    val stringNodeBuilder = NewNodeBuilder<String>()

    // plain case
    val stringAnnotation = PlainAnnotation<String>()
    stringNodeBuilder.addAnnotation(stringAnnotation)

    // Annotation<Any?> can assume nothing about F, and can't be an Annotation<Int> so this is safe
    val anyAnnotation = PlainAnnotation<Any?>()
    stringNodeBuilder.addAnnotation(anyAnnotation)

    // this can't happen, so you can't accidentally assume the fixture type
//    val stringAsAnyAnnotation: NewAnnotation<Any?> = PlainAnnotation<String>()

    // Obviously we don't want this
//    stringNodeBuilder.addAnnotation(PlainAnnotation<Int>())

    // Annotation<*> could be anything, so be Annotation<Int> and assume that F is Int
    val starAnnotation: NewAnnotation<*> = PlainAnnotation<Int>()
//    stringNodeBuilder.addAnnotation(starAnnotation)

    // an Int annotation can't be attached to a number node
    val numberNodeBuilder = NewNodeBuilder<Number>()
//    numberNodeBuilder.addAnnotation(PlainAnnotation<Int>())

    // a number annotation can be attached to an int node
    val intNodeBuilder = NewNodeBuilder<Int>()
    intNodeBuilder.addAnnotation(PlainAnnotation<Number>())

    val list1: List<NewAnnotation<String>> = stringAnnotation + stringAnnotation
    val list2: List<NewAnnotation<Int>> = listOf(PlainAnnotation<Int>()) + PlainAnnotation<Number>()
    val list3: List<NewAnnotation<Int>> = PlainAnnotation<Int>() + PlainAnnotation<Number>()
    val list4: List<NewAnnotation<Int>> = PlainAnnotation<Int>() + PlainAnnotation<Any?>()
    val list5: List<NewAnnotation<Int>> = PlainAnnotation<Any?>() + PlainAnnotation<Any?>()
    val list6: List<NewAnnotation<Int>> = PlainAnnotation<Any?>() + PlainAnnotation<Int>()

// list7 used to compile under 1.3.7, but doesn't under 1.4.21
//    val list7: List<NewAnnotation<*>> = PlainAnnotation<String>() + PlainAnnotation<Int>()

//    val list8: List<NewAnnotation<Any?>> = PlainAnnotation<String>() + PlainAnnotation<Int>()

    val list9: List<NewAnnotation<String>> = PlainAnnotation<String>() + PlainAnnotation<String>() + PlainAnnotation<String>()
    val list10: List<NewAnnotation<Int>> = PlainAnnotation<Int>() + PlainAnnotation<Int>() + PlainAnnotation<Number>()
    val list11: List<NewAnnotation<*>> = PlainAnnotation<Int>() + PlainAnnotation<Int>() + PlainAnnotation<String>()
}
