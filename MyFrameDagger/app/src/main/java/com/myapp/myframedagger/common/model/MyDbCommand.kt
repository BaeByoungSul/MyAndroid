package com.myapp.myframedagger.common.model
data class MyDbCommand(
    var commandName: String ,
    var connectionName: String ,
    var commandType: Int ,
    var commandText: String ,
    var parameters: MutableList<MyPara>? = null,
    //var paraValues: MutableList<Map<String, String>>? = null
    var paraValues: MutableList<HashMap<String, String>>? = null
)
data class MyPara(
    var parameterName: String = "",
    var dbDataType: Int = 0,
    var direction: Int = 0,
    var headerCommandName: String = "",
    var headerParameter: String = ""
)
enum class CommandType( val type: Int ){
    Text (1),
    StoredProcedure (4),
    TableDirect ( 512)
}
enum class ParameterDirection( val type: Int ){
    Input (1),
    Output (2),
    InputOutput (3),
    ReturnValue ( 6)
}
// 필요 시 SqlDbType에서 참조에서 추가
enum class MsSqlDataType(val type: Int) {
    BigInt(0),
    Binary(1),
    Char(3),
    DateTime(4),
    Decimal(5),
    Image(7),
    Int16(8),
    NVarchar(12),
    SmallDateTime(15),
    Varchar(22)
}
enum class OracleDbType(val type: Int){
    Char(104),
    Date(106),
    Decimal(107),
    Double(104),
    Int32(112),
    NChar(117),
    NVarchar2(119),
    RefCursor(121),
    Varchar2(126)
}