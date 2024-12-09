import com.example.springboot.app.asset.AssetService
import com.example.springboot.app.service.PrintScriptService
import com.example.springboot.app.utils.RunTestDTO
import com.printscript.interpreter.interfaces.InterpreterResult
import com.printscript.interpreter.results.InterpreterSuccess
import com.printscript.runner.Runner
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class PrintScriptServiceTest {

    @Mock
    lateinit var assetService: AssetService

    @InjectMocks
    lateinit var printScriptService: PrintScriptService

    private val mockFile = MockMultipartFile("file", "content".toByteArray())

    @Test
    fun `should validate snippet successfully`() {
        `when`(assetService.getSnippet("snippetId")).thenReturn(ResponseEntity.ok(mockFile))

        val result = printScriptService.validateSnippet("1.1", "snippetId")
        assertEquals(null, result.error)
    }

    @Test
    fun `should return error when validation fails`() {
        `when`(assetService.getSnippet("snippetId")).thenThrow(RuntimeException("Asset not found"))

        val result = printScriptService.validateSnippet("1.1", "snippetId")
        assertEquals("Asset not found", result.error)
    }


    @Test
    fun `should return error when execution fails`() {
        `when`(assetService.getSnippet("snippetId")).thenThrow(RuntimeException("Execution error"))

        val result = printScriptService.executeSnippet("1.1", "snippetId")
        assertEquals("Execution error", result.error)
    }

    @Test
    fun `should execute snippet test successfully`() {
        val runTestDTO = RunTestDTO("input", null, emptyList(), emptyList(), null)
        `when`(assetService.getSnippet("snippetId")).thenReturn(ResponseEntity.ok(mockFile))

        val result = printScriptService.executeSnippetTest("1.1", "snippetId", runTestDTO)
        assertNotNull(result.error)
    }

    @Test
    fun `should return error when snippet test execution fails`() {
        `when`(assetService.getSnippet("snippetId")).thenThrow(RuntimeException("Execution error"))

        val runTestDTO = RunTestDTO("input", null, emptyList(), emptyList(), null)
        val result = printScriptService.executeSnippetTest("1.1", "snippetId", runTestDTO)
        assertEquals("Execution error", result.error)
    }

    @Test
    fun `should fetch multipart file successfully`() {
        `when`(assetService.getSnippet("snippetId")).thenReturn(ResponseEntity.ok(mockFile))

        val file = printScriptService.fetchMultipartFile("snippetId")
        assertNotNull(file)
    }

    @Test
    fun `should generate file from multipart`() {
        val file = printScriptService.genFile(mockFile, "ps")
        assertNotNull(file)
    }

    @Test
    fun `should generate multipart file from file`() {
        val tempFile = printScriptService.genFile(mockFile, "ps")
        val multipartFile = printScriptService.genMultiPartFile(tempFile)
        assertNotNull(multipartFile)
    }
}
