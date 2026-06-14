package com.folio.reader.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccountsTest {

    private fun acc(id: String, name: String = "u", token: String = "t") =
        Account(id = id, serverUrl = "http://h", userName = name, token = token)

    @Test
    fun `idFor is stable across equivalent server urls`() {
        assertEquals(
            Accounts.idFor("http://alan-mint:8280", "test"),
            Accounts.idFor("http://alan-mint:8280/", "test"),
        )
    }

    @Test
    fun `idFor distinguishes users on the same server`() {
        assert(Accounts.idFor("http://h", "a") != Accounts.idFor("http://h", "b"))
    }

    @Test
    fun `upsert adds a new account`() {
        val list = Accounts.upsert(emptyList(), acc("a"))
        assertEquals(1, list.size)
        assertEquals("a", list[0].id)
    }

    @Test
    fun `upsert replaces an existing account by id and keeps position`() {
        val start = listOf(acc("a", token = "old"), acc("b"))
        val updated = Accounts.upsert(start, acc("a", token = "new"))
        assertEquals(2, updated.size)
        assertEquals("new", updated[0].token)
        assertEquals("b", updated[1].id)
    }

    @Test
    fun `remove drops the matching account`() {
        val list = Accounts.remove(listOf(acc("a"), acc("b")), "a")
        assertEquals(listOf("b"), list.map { it.id })
    }

    @Test
    fun `resolveActive prefers the matching id`() {
        val list = listOf(acc("a"), acc("b"))
        assertEquals("b", Accounts.resolveActive(list, "b")?.id)
    }

    @Test
    fun `resolveActive falls back to first when id is missing or unknown`() {
        val list = listOf(acc("a"), acc("b"))
        assertEquals("a", Accounts.resolveActive(list, null)?.id)
        assertEquals("a", Accounts.resolveActive(list, "zzz")?.id)
    }

    @Test
    fun `resolveActive returns null for an empty list`() {
        assertNull(Accounts.resolveActive(emptyList(), "a"))
    }
}
