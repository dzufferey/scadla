package scadla.backends.x3d

import scadla._
import scadla.backends.Viewer
import dzufferey.utils.SysCmd
import java.io._
import java.awt.Desktop

object X3DomViewer extends Viewer {

  //TODO change the default camera position

  // give enough time for the browser to start and load the file (in second)
  var sleepTime = 5

  val prefix = """<!DOCTYPE html>
                 |<html>
                 |<head>
                 |    <meta charset='UTF-8'>
                 |    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
                 |    <title>Scadla X3Dom Viewer</title>
                 |    <script type='text/javascript' src='https://www.x3dom.org/download/x3dom.js'> </script>
                 |    <link rel='stylesheet' type='text/css' href='https://www.x3dom.org/download/x3dom.css'>
                 |    <style>
                 |    #mainViewer {
                 |        background: rgba(64, 64, 196, 0.4);
                 |        width: 99vw;
                 |        height: 99vh;
                 |    }
                 |    #axesSmallAll {
                 |        position: absolute;
                 |        width: 80px;
                 |        height: 80px;
                 |        right: 80px;
                 |        bottom: 40px;
                 |        border: none;
                 |        z-index: 1000;
                 |    }
                 |    #config {
                 |        position: absolute;
                 |        left: 10px;
                 |        bottom: 30px;
                 |        border: none;
                 |    }
                 |    </style>
                 |</head>
                 |<body>
                 |<x3d id='mainViewer' width='98vw' height='98vh'>
                 |    <scene>
                 |        <viewpoint id="viewPoint" position="5.53912 7.69774 6.54642" orientation="-0.69862 0.66817 0.25590 1.00294"></viewpoint>
                 |        <navigationInfo id="navi" type='"TURNTABLE" "ANY"' typeParams="-0.4, 60, 0.5, 1.55"></navigationInfo>
                 |        <transform rotation='1 0 0 -1.570796326795'>""".stripMargin

  //contains parts from
  //- https://github.com/x3dom/component-editor/blob/master/static/x3d/plane.x3d
  //- https://github.com/x3dom/component-editor/blob/master/static/x3d/axes.x3d
  def viewerSuffix= """        </transform>
                      |        <transform id="plane"  mapDEFToID="true" namespaceName="plane" rotation="1 0 0 -1.57079632679">
                      |            <Shape id="planeGrid" isPickable="false">
                      |                <Appearance DEF="GRID_APPEARANCE" sortKey="-1">
                      |                    <Material diffuseColor='0.0 0.0 0.0' specularColor='0.0 0.0 0.0' emissiveColor='0.3 0.3 0.3'></Material>
                      |                    <DepthMode id="depthMode" readOnly="false"></DepthMode>
                      |                </Appearance>
                      |                <Plane id="gridPlane" solid="false" size='20 20' primType='LINES' subdivision='20 20'>
                      |                </Plane>
                      |            </Shape>
                      |            <Shape id="planeBorder" isPickable="false">
                      |                <Appearance >
                      |                    <Material diffuseColor='0.0 0.0 0.0' specularColor='0.0 0.0 0.0'  emissiveColor='0.3 0.3 0.3'></Material>
                      |                    <DepthMode  readOnly="false"></DepthMode>
                      |                </Appearance>
                      |                <IndexedLineSet coordIndex="0 1 2 3 0 -1" colorPerVertex="false" lit="false">
                      |                    <Coordinate id="gridBordersCoordNode" point="-10.0 -10.0 0.0 , -10.0 10.0 0.0 , 10.0 10.0 0.0, 10.0 -10.0 0.0"/>
                      |                </IndexedLineSet>
                      |            </Shape>
                      |        </transform>
                      |        <group id="axes" mapDEFToID="true" namespaceName="plane">
                      |            <!-- X arrow and label -->
                      |            <Shape isPickable="false" DEF="AXIS_LINE_X">
                      |                <IndexedLineSet index="0 1 -1">
                      |                    <Coordinate point="0 0.001 0, 15 0.001 0" color="1 0 0, 1 0 0"></Coordinate>
                      |                </IndexedLineSet>
                      |                <Appearance DEF='Red'>
                      |                    <Material diffuseColor="0 0 0" emissiveColor='1 0 0'></Material>
                      |                    <DepthMode  readOnly="false"></DepthMode>
                      |                    <LineProperties linewidthScaleFactor="2.0"></LineProperties>
                      |                </Appearance>
                      |            </Shape>
                      |            <!-- Y arrow and label -->                            
                      |            <Shape isPickable="false" DEF="AXIS_LINE_Y">
                      |                <IndexedLineSet index="0 1 -1">
                      |                    <Coordinate point="0 0.001 0, 0 0.001 -15" color="0 1 0, 0 1 0"></Coordinate>
                      |                </IndexedLineSet>
                      |                <Appearance DEF='Green'>
                      |                    <Material diffuseColor="0 0 0" emissiveColor='0 1 0'></Material>
                      |                    <LineProperties linewidthScaleFactor="2.0"></LineProperties>
                      |                    <DepthMode  readOnly="false"></DepthMode>
                      |                </Appearance>
                      |            </Shape>
                      |            <!-- Z arrow and label -->
                      |            <Shape isPickable="false" DEF="AXIS_LINE_Z">
                      |                <IndexedLineSet index="0 1 -1" >
                      |                    <Coordinate point="0 0 0, 0 15 0" color="0 0 1, 0 0 1"></Coordinate>
                      |                </IndexedLineSet>
                      |                <Appearance DEF='Blue'>
                      |                    <Material diffuseColor="0 0 0" emissiveColor='0 0 1'></Material>
                      |                    <LineProperties linewidthScaleFactor="2.0"></LineProperties>
                      |                </Appearance>
                      |            </Shape>
                      |            <!-- Z height cue -->
                      |            <Shape isPickable="false" DEF="AXIS_Z_CUE">
                      |                <Sphere radius='0.05'/>
                      |                <Appearance >
                      |                    <Material diffuseColor="0 0 0" emissiveColor='0 0 1'></Material>
                      |                </Appearance>
                      |            </Shape>
                      |        </group>
                      |    </scene>
                      |</x3d>""".stripMargin

  //modified from https://github.com/x3dom/component-editor/blob/master/static/x3d/axesSmall.x3d
  //TODO make them rotate with the main view, see https://github.com/x3dom/component-editor/blob/master/static/js/jquery.viewConnector.js
  val smallAxis = """<X3D id='axesSmallAll' mapDEFToID="true" namespaceName="axes">
                    |    <scene>
                    |    <navigationInfo type='"NONE" "ANY"'></navigationInfo>
                    |    <viewpoint position="1.7 2.8 2.0"  orientation="-0.69862 0.66817 0.25590 1.00294"></viewpoint>
                    |    <transform rotation="1 0 0 -1.57079632679">
                    |    <transform>
                    |        <group id='axesSmall'>
                    |            <!-- X arrow and label -->
                    |            <Shape isPickable="false" DEF="AXIS_LINE_X">
                    |                <IndexedLineSet index="0 1 -1">
                    |                    <Coordinate point="0 0 0.001, 1 0 0.001" color="1 0 0, 1 0 0"></Coordinate>
                    |                </IndexedLineSet>
                    |                <Appearance DEF='Red'>
                    |                    <Material diffuseColor="0 0 0" emissiveColor='1 0 0'></Material>
                    |                </Appearance>
                    |            </Shape>
                    |            <Transform translation='1 0 0'>
                    |                <Transform rotation='0 0 1 -1.57079632679'>
                    |                    <Shape isPickable="false" DEF="AXIS_ARROW_X">
                    |                        <Cone DEF='ArrowCone' bottomRadius='.10' height='0.5' subdivision="16"></Cone>
                    |                        <Appearance USE='Red'></Appearance>
                    |                    </Shape>
                    |                </Transform>
                    |                <Transform rotation='1 0 0 1.57079632679' translation='0.5 0 0'>
                    |                    <Billboard>
                    |                        <Shape isPickable="false" DEF="AXIS_LABEL_X">
                    |                            <Text string="X" solid="false">
                    |                                <FontStyle size="0.6"></FontStyle>
                    |                            </Text>
                    |                            <Appearance USE='Red'></Appearance>
                    |                        </Shape>
                    |                    </Billboard>
                    |                </Transform>
                    |            </Transform>
                    |            <!-- Y arrow and label -->                            
                    |            <Shape isPickable="false" DEF="AXIS_LINE_Y">
                    |                <IndexedLineSet index="0 1 -1">
                    |                    <Coordinate point="0 0 0.001, 0 1 0.001" color="0 1 0, 0 1 0"></Coordinate>
                    |                </IndexedLineSet>
                    |                <Appearance DEF='Green'>
                    |                    <Material diffuseColor="0 0 0" emissiveColor='0 1 0'></Material>
                    |                </Appearance>
                    |            </Shape>
                    |            <Transform translation='0 1 0'>
                    |                <Shape isPickable="false" DEF="AXIS_ARROW_Y">
                    |                    <Cone USE='ArrowCone'></Cone>
                    |                    <Appearance USE='Green'></Appearance>
                    |                </Shape>
                    |                <Transform rotation='1 0 0 1.57079632679' translation='0 0.5 0'>
                    |                    <Billboard>
                    |                        <Shape isPickable="false" DEF="AXIS_LABEL_Y">
                    |                            <Text string="Y" solid="false">
                    |                                <FontStyle size="0.6"></FontStyle>
                    |                            </Text>
                    |                            <Appearance USE='Green'></Appearance>
                    |                        </Shape>
                    |                    </Billboard>
                    |                </Transform>
                    |            </Transform>
                    |            <!-- Z arrow and label -->
                    |            <Shape isPickable="false" DEF="AXIS_LINE_Z">
                    |                <IndexedLineSet index="0 1 -1">
                    |                    <Coordinate point="0 0 0.001, 0 0 1" color="0 0 1, 0 0 1"></Coordinate>
                    |                </IndexedLineSet>
                    |                <Appearance DEF='Blue'>
                    |                    <Material diffuseColor="0 0 0" emissiveColor='0 0 1'></Material>
                    |                </Appearance>
                    |            </Shape>
                    |            <Transform translation='0 0 1'>
                    |                <Transform rotation='1 0 0 1.57079632679'>
                    |                    <Shape isPickable="false" DEF="AXIS_ARROW_Z">
                    |                        <Cone USE='ArrowCone'></Cone>
                    |                        <Appearance USE='Blue'></Appearance>
                    |                    </Shape>
                    |                    <Transform translation='0 0.5 0'>
                    |                    <Billboard>
                    |                        <Shape isPickable="false" DEF="AXIS_LABEL_Z">
                    |                            <Text string="Z" solid="false">
                    |                                <FontStyle size="0.6"></FontStyle>
                    |                            </Text>
                    |                            <Appearance USE='Blue'></Appearance>
                    |                        </Shape>
                    |                    </Billboard>
                    |                    </Transform>
                    |                </Transform>
                    |            </Transform>
                    |        </group>
                    |    </transform>
                    |    </transform>
                    |    </scene>
                    |</X3D>""".stripMargin


  val opts = """<script type="text/javascript">
                |function toggleVisibility(shapeId, checkbox) {
                |    var shape = document.getElementById(shapeId);
                |    if (checkbox.checked) shape.setAttribute("render", true);
                |    else shape.setAttribute("render", false);
                |};
                |function updateGrid() {
                |    var size = document.getElementById("gridSize").value;
                |    var tick = document.getElementById("gridTick").value;
                |    var plane = document.getElementById("gridPlane");
                |    size = Math.ceil(size/tick) * tick;
                |    plane.size=(2*size) + " " + (2*size);
                |    plane.subdivision=(2*size/tick) + " " + (2*size/tick);
                |    var border = document.getElementById("gridBordersCoordNode");
                |    border.point= (-size) + " " + (-size) + " 0.0 , " +
                |                  (-size) + " " + ( size) + " 0.0 , " +
                |                  ( size) + " " + ( size) + " 0.0 , " +
                |                  ( size) + " " + (-size) + " 0.0";
                |};
                |</script>
                |<div id="config">
                |    <form>
                |        <input type="checkbox" id="axesCheckbox" checked="checked" onchange='toggleVisibility("axes", this); toggleVisibility("axesSmall", this);'/>
                |        <label for="axesCheckbox">Axes</label>
                |        <br/>
                |        <input type="checkbox" id="gridCheckbox" checked="checked" onchange='toggleVisibility("plane", this);'/>
                |        <label for="gridCheckbox">Grid</label>
                |        <br/>
                |        <label for="gridSize">Grid Size:</label>
                |        <input type="number" id="gridSize" value="10" min="0" style="width: 50px;" onchange='updateGrid();'>
                |        <br/>
                |        <label for="gridTick">Grid Tick:</label>
                |        <input type="number" id="gridTick" value="1" step="any" min="0" style="width: 50px;" onchange='updateGrid();'>
                |        <br/>
                |    </form>
                |</div>""".stripMargin
                  
  val suffix = """</body>
                 |</html>""".stripMargin

  //the files are cleaned only when the JVM terminate
  def apply(obj: Polyhedron): Unit = {
    val tmpFile = java.io.File.createTempFile("scadlaModel", ".html")
    tmpFile.deleteOnExit
    val writer = new BufferedWriter(new FileWriter(tmpFile))
    writer.write(prefix)
    writer.newLine
    Printer.write(obj, writer, onlyShape = true, withHeader = false)
    writer.write(viewerSuffix)
    writer.newLine
    writer.write(smallAxis)
    writer.newLine
    writer.write(opts)
    writer.newLine
    writer.write(suffix)
    writer.newLine
    writer.close
    Desktop.getDesktop().open(tmpFile)
    Thread.sleep(sleepTime * 1000)
  }

  lazy val isPresent = {
    Desktop.isDesktopSupported && Desktop.getDesktop.isSupported(Desktop.Action.OPEN)
  }

}
