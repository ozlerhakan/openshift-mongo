<!DOCTYPE html>
<html>
<head>
    <title>Bookgram</title>
</head>
<body>

<#list books as book>
 <#if book["thumbnailUrl"]??>
    <a href="${book["thumbnailUrl"]}"><img src="${book["thumbnailUrl"]}" title="${book["title"]}" width="100" height="130" border="0"></a>
 </#if>
</#list>
</body>
</html>