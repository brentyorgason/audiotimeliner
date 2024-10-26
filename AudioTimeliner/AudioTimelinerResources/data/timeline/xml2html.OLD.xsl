<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">
<xsl:output method="html" indent="yes" encoding="utf-8"/>

<xsl:param name="fontString"/>
<xsl:param name="Annotation"/>
<xsl:param name="time"/>

<xsl:template match="/">
  <html>
    <head>
      <title>Timeline</title>
      <style><xsl:value-of select="$fontString"/></style>
    </head>
    <body>
       <xsl:apply-templates select="/Timeline"/>
    </body>
  </html>
</xsl:template>

<xsl:template match="Timeline">
    <p><dt><b><xsl:value-of select="@title"/></b></dt></p>
    <dt><img width = "660">
        <xsl:attribute name='src'>
            <xsl:value-of select="GifName"/>
        </xsl:attribute>
    </img></dt>
    <dl><xsl:apply-templates select="Bubble|Timepoints"/>
    </dl>
    <font size = "2">
    <blockquote>
    <xsl:text>Audio source: </xsl:text>
    <xsl:value-of select="@mediaContent"/>
    <xsl:text>; Length: </xsl:text>
    <xsl:value-of select="@mediaLength"/>
    <xsl:text>; Offset in audio: </xsl:text>
    <xsl:value-of select="@mediaOffset"/>
    </blockquote>
    </font>
    <p><xsl:value-of select="Annotation"/></p>
</xsl:template>

<xsl:template match="Bubble">
    <dt>
    <b><xsl:value-of select="@label"/></b>
    <xsl:if test="@time!=''">
    <xsl:text> [</xsl:text>
    <xsl:value-of select="@time"/>
    <xsl:text>] </xsl:text>
    <xsl:text>  </xsl:text>
    </xsl:if>
    <xsl:value-of select="Annotation"/>
    </dt>
    <dl>
    <xsl:apply-templates select="Bubble|Timepoints"/>
    </dl>
</xsl:template>

<xsl:template match="Timepoints">
    <blockquote>
    <font size = "2">
    <xsl:text>Timepoint list (in milliseconds): [</xsl:text>
    <xsl:apply-templates select="Timepoint"/>
    <xsl:text>]</xsl:text>
    </font>
    </blockquote>
</xsl:template>

<xsl:template match="Timepoint">
    <xsl:value-of select="@offset"/>
    <xsl:if test="$Annotation!=''">
      <xsl:text>  </xsl:text>
      <xsl:value-of select="Annotation"/>
    </xsl:if>
    <xsl:if test="position()!=last()">
      <xsl:text>, </xsl:text>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>

