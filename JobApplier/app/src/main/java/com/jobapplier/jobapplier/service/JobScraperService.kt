package com.jobapplier.jobapplier.service

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class Job(val email: String, val link: String)

object JobScraperService {
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1"
    fun getJobs(jobTitle: String, location: String): List<Job> {
        fun addJobs(document: Document): List<Job> {
            fun extractEmail(jobDescription: String): String {
                val email =
                        Regex("[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*",
                                RegexOption.IGNORE_CASE).find(jobDescription)
                return email?.value ?: ""
            }

            return document.select(".btnView").map { it ->
                val link = "http://www.jobmail.co.za${it.attr("href")}"
                val jobPageDetail = loadWebPage(link).parse()
                val jobDescription = jobPageDetail.select(".jobDescription").firstOrNull()?.text() ?: "".trim()
                val email = extractEmail(jobDescription)
                Job(email, link)
            }
        }

        val url = "http://www.jobmail.co.za/job-search/${jobTitle.replace(" ", "+")}/all-industries/all-sub-industries/$location/all-jobs/page1/matchany/sortrelevance"
        return addJobs(loadWebPage(url).parse()).filter { it.email.isNotEmpty() }
    }

    private fun loadWebPage(url: String): Connection.Response {
        return Jsoup.connect(url).ignoreContentType(true).userAgent(USER_AGENT).execute()
    }
}
