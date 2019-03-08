package dev.minutest.experimental


// Experiments with the typesystem outside the current types

class NewNode<F>(
    val annotations: List<NewAnnotation<in F>>
)

class NewNodeBuilder<F> {

    private val annotations: MutableList<NewAnnotation<F>> = mutableListOf()

    fun addAnnotation(a: NewAnnotation<in F>) {
        annotations.add(a as NewAnnotation<F>)
    }

    fun build(): NewNode<out F> {
        val base = NewNode(annotations)
        return if (annotations.isEmpty())
            base
        else
            annotations.first().transform(base)
    }
}

interface NewAnnotation<F> {
    fun transform(node: NewNode<F>): NewNode<F> = node
}

class PlainAnnotation<F> : NewAnnotation<F>

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
}
