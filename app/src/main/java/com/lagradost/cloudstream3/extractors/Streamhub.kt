package com.lagradost.cloudstream3.extractors

import com.lagradost.cloudstream3.network.get
import com.lagradost.cloudstream3.network.text
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.JsUnpacker
import com.lagradost.cloudstream3.utils.Qualities
import java.net.URI

class Streamhub : ExtractorApi() {
    override val mainUrl: String
        get() = "https://streamhub.to"
    override val name: String
        get() = "Streamhub"
    override val requiresReferer: Boolean
        get() = false

    override fun getExtractorUrl(id: String): String {
        return "$mainUrl/e/$id"
    }

    override fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val response = get(url).text
        Regex("eval((.|\\n)*?)</script>").find(response)?.groupValues?.get(1)?.let { jsEval ->
            JsUnpacker("eval$jsEval" ).unpack()?.let { unPacked ->
                Regex("sources:\\[\\{src:\"(.*?)\"").find(unPacked)?.groupValues?.get(1)?.let { link ->
                    return listOf(
                        ExtractorLink(
                            this.name,
                            this.name,
                            link,
                            referer ?: "",
                            Qualities.Unknown.value,
                            URI(link).path.endsWith(".m3u8")
                        )
                    )
                }
            }
        }
        return null
    }
}