package decoder

object CheckFile {
  val func =
    """function sz_T2S(_t)
      |    function catch(what)
      |        return what[1]
      |     end
      |
      |     function try(what)
      |        status, result = pcall(what[1])
      |        if not status then
      |           what[2](result)
      |        end
      |        return result
      |     end
      |
      |    local szRet = "{"
      |    function doT2S(_i, _v)
      |        if "number" == type(_i) then
      |            szRet = szRet .. '"' .. _i .. '" : '
      |            if "number" == type(_v) then
      |                szRet = szRet .. _v .. ","
      |            elseif "string" == type(_v) then
      |                szRet = szRet .. '"' .. _v .. '"' .. ","
      |            elseif "table" == type(_v) then
      |                szRet = szRet .. sz_T2S(_v) .. ","
      |            else
      |                szRet = szRet .. "nil,"
      |            end
      |        elseif "string" == type(_i) then
      |            szRet = szRet .. '"' .. _i .. '" : '
      |            if "number" == type(_v) then
      |                szRet = szRet .. _v .. ","
      |            elseif "string" == type(_v) then
      |                szRet = szRet .. '"' .. _v .. '"' .. ","
      |            elseif "table" == type(_v) then
      |                szRet = szRet .. sz_T2S(_v) .. ","
      |            else
      |                szRet = szRet .. "nil,"
      |            end
      |        end
      |    end
      |    if type(_t) == "table" then
      |        table.foreach(_t, doT2S)
      |        szRet = szRet .. "}"
      |        return szRet
      |    else
      |        return tostring(_t)
      |    end
      |
      |end
      |
      |function __text_write_to_file(s)
      |    s = s:gsub(".lua","")
      |    _texts = require(s)
      |    _file = io.open ("D:/nyhx/text" .. s ,"a")
      |    _file:write(sz_T2S(_texts))
      |    _file:close()
      |end
      |
    """.stripMargin

  def main(args: Array[String]): Unit = {
    import better.files._
    val s = File("F:\\software\\android\\res-decrypt\\codes\\shared\\conftable").listRecursively
      .toList
      .sortBy(_.size)
      .filterNot(_.isDirectory)
      .map(e => e.name)
      .map(_.replace("luac", "lua"))
      .map(s =>s""" __text_write_to_file("$s")""")
      .filterNot(_.startsWith("__"))
      .filterNot(_.startsWith("enums"))
      .filterNot(_.startsWith("a.lua"))


    File("tmp")
      .writeText(func)
      .appendText(s.mkString("\n"))
    //      .map(e => e.pathAsString -> (e.size / 1024))
    //      .foreach(println)
  }
}
