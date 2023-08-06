data class Trans(val comment: String, var ctxt: String?, var id: String?, var str: String?) {
    constructor(comment: String) : this(comment = comment, ctxt = null, id = null, str = null)
}