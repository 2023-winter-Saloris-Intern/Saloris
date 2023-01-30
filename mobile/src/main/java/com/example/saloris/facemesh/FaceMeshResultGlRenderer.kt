package com.example.saloris.facemesh

import android.opengl.GLES20
import com.example.saloris.util.*
import com.google.common.collect.ImmutableSet
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark
import com.google.mediapipe.solutioncore.ResultGlRenderer
import com.google.mediapipe.solutions.facemesh.FaceMesh
import com.google.mediapipe.solutions.facemesh.FaceMeshConnections
import com.google.mediapipe.solutions.facemesh.FaceMeshResult
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FaceMeshResultGlRenderer(
    faceMeshSettings: BooleanArray, faceMeshColors: ArrayList<FloatArray>
) : ResultGlRenderer<FaceMeshResult> {
    private var program = 0
    private var positionHandle = 0
    private var projectionMatrixHandle = 0
    private var colorHandle = 0

    private var faceMeshSettings = object {
        var eye = faceMeshSettings[0]
        var eyeBrow = faceMeshSettings[1]
        var eyePupil = faceMeshSettings[2]
        var lib = faceMeshSettings[3]
        var faceMesh = faceMeshSettings[4]
        var faceLine = faceMeshSettings[5]
    }
    private var faceMeshColors = object {
        var eyeColor = faceMeshColors[0]
        var eyeBrowColor = faceMeshColors[1]
        var eyePupilColor = faceMeshColors[2]
        var libColor = faceMeshColors[3]
        var faceMeshColor = faceMeshColors[4]
        var faceLineColor = faceMeshColors[5]
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    override fun setupRendering() {
        program = GLES20.glCreateProgram()
        val vertexShader =
            loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader =
            loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        projectionMatrixHandle = GLES20.glGetUniformLocation(program, "uProjectionMatrix")
        colorHandle = GLES20.glGetUniformLocation(program, "uColor")
    }

    override fun renderResult(result: FaceMeshResult?, projectionMatrix: FloatArray?) {
        if (result == null) {
            return
        }
        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0)

        val numFaces = result.multiFaceLandmarks().size

        for (i in 0 until numFaces) {
            if (faceMeshSettings.faceMesh) {
                drawLandmarks(
                    result.multiFaceLandmarks()[i].landmarkList,
                    FaceMeshConnections.FACEMESH_TESSELATION,
                    faceMeshColors.faceMeshColor,
                    TESSELATION_THICKNESS
                )
            }
            if (faceMeshSettings.eye) {
                drawLandmarks(
                    result.multiFaceLandmarks()[i].landmarkList,
                    FaceMeshConnections.FACEMESH_RIGHT_EYE,
                    faceMeshColors.eyeColor,
                    RIGHT_EYE_THICKNESS
                )
                drawLandmarks(
                    result.multiFaceLandmarks()[i].landmarkList,
                    FaceMeshConnections.FACEMESH_LEFT_EYE,
                    faceMeshColors.eyeColor,
                    LEFT_EYE_THICKNESS
                )
            }
            if (faceMeshSettings.eyeBrow) {
                drawLandmarks(
                    result.multiFaceLandmarks()[i].landmarkList,
                    FaceMeshConnections.FACEMESH_RIGHT_EYEBROW,
                    faceMeshColors.eyeBrowColor,
                    RIGHT_EYEBROW_THICKNESS
                )
                drawLandmarks(
                    result.multiFaceLandmarks()[i].landmarkList,
                    FaceMeshConnections.FACEMESH_LEFT_EYEBROW,
                    faceMeshColors.eyeBrowColor,
                    LEFT_EYEBROW_THICKNESS
                )
            }
            if (faceMeshSettings.faceLine) {
                drawLandmarks(
                    result.multiFaceLandmarks()[i].landmarkList,
                    FaceMeshConnections.FACEMESH_FACE_OVAL,
                    faceMeshColors.faceLineColor,
                    FACE_OVAL_THICKNESS
                )
            }
            if (faceMeshSettings.lib) {
                drawLandmarks(
                    result.multiFaceLandmarks()[i].landmarkList,
                    FaceMeshConnections.FACEMESH_LIPS,
                    faceMeshColors.libColor,
                    LIPS_THICKNESS
                )
            }
            if (faceMeshSettings.eyePupil) {
                if (result.multiFaceLandmarks()[i].landmarkCount == FaceMesh.FACEMESH_NUM_LANDMARKS_WITH_IRISES) {
                    drawLandmarks(
                        result.multiFaceLandmarks()[i].landmarkList,
                        FaceMeshConnections.FACEMESH_RIGHT_IRIS,
                        faceMeshColors.eyePupilColor,
                        RIGHT_EYE_THICKNESS
                    )
                    drawLandmarks(
                        result.multiFaceLandmarks()[i].landmarkList,
                        FaceMeshConnections.FACEMESH_LEFT_IRIS,
                        faceMeshColors.eyePupilColor,
                        LEFT_EYE_THICKNESS
                    )
                }
            }
        }
    }

    /**
     * Deletes the shader program.
     * This is only necessary if one wants to release the program while keeping the context around.
     */
    fun release() {
        GLES20.glDeleteProgram(program)
    }

    private fun drawLandmarks(
        faceLandmarkList: List<NormalizedLandmark>,
        connections: ImmutableSet<FaceMeshConnections.Connection>,
        colorArray: FloatArray,
        thickness: Int
    ) {
        GLES20.glUniform4fv(colorHandle, 1, colorArray, 0)
        GLES20.glLineWidth(thickness.toFloat())
        for (c in connections) {
            val start = faceLandmarkList[c.start()]
            val end = faceLandmarkList[c.end()]
            val vertex = floatArrayOf(start.x, start.y, end.x, end.y)
            val vertexBuffer = ByteBuffer.allocateDirect(vertex.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertex)
            vertexBuffer.position(0)
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2)
        }
    }
}
