class FaselHD : MainAPI() {
    override var name = "FaselHD"
    override var mainUrl = "https://web185.faselhd.cafe/"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/search/${query.replace(" ", "+")}/"
        val doc = app.get(url).document

        return doc.select(".Grid--WecimaPosts .WecimaPostItem").mapNotNull {
            it.toSearchResponse()
        }
    }

    private fun Element.toSearchResponse(): SearchResponse? {
        val title = this.selectFirst(".Title")?.text() ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val poster = fixUrlNull(this.selectFirst("img")?.attr("src"))
        val isMovie = href.contains("/movie/")

        return if (isMovie) {
            MovieSearchResponse(
                title, href, this@FaselHD.name, TvType.Movie, poster
            )
        } else {
            TvSeriesSearchResponse(
                title, href, this@FaselHD.name, TvType.TvSeries, poster
            )
        }
    }

    // ✅ لازم يكونوا داخل الكلاس
    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst(".Movie--Title")?.text() ?: "FaselHD"
        val poster = fixUrlNull(doc.selectFirst(".Movie--Poster img")?.attr("src"))
        val description = doc.selectFirst(".Description")?.text()
        val tags = doc.select(".Movie--Genres a").map { it.text() }
        val year = doc.selectFirst(".Movie--Meta .Date")?.text()?.take(4)?.toIntOrNull()

        val episodes = doc.select(".Episodes--Seasons .Episode").mapNotNull {
            val name = it.text()
            val link = it.attr("href")
            Episode(fixUrl(link), name)
        }

        return if (url.contains("/series/")) {
            TvSeriesLoadResponse(
                title, url, this.name, TvType.TvSeries, episodes, poster, year, description, null, tags
            )
        } else {
            MovieLoadResponse(
                title, url, this.name, TvType.Movie, url, poster, year, description, null, tags
            )
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        val iframe = doc.selectFirst("iframe")?.attr("src") ?: return false

        val extractor = loadExtractor(iframe, data, subtitleCallback, callback)
        return extractor
    }
}
