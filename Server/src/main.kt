import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Exception
import java.net.ServerSocket

fun main() {
    try {

        while (true) {
            //먼저 서버 소켓의 객체를 생성해줍니다. ServerSocket 안에는 포트 번호를 넣어줍니다
            val port = 9999 // 사용할 포트 번호
            val server = ServerSocket(port)
            println("Server started on port $port")

            println("사용자 접속 대기중...")

            while (true) {
                val socket = serverSocket.accept() // 클라이언트와 연결 대기
                val inputReader = BufferedReader(InputStreamReader(socket.getInputStream())) // 입력 스트림 생성
                val inputLine = inputReader.readLine() // 클라이언트로부터 받은 데이터 읽기
                println("Received message from client: $inputLine")

                socket.close() // 소켓 닫기
            }

            //Socket을 연결
            val socket = server.accept()
            //Socket을 통해 데이터를 얻어오기 위한 코드
            val input = socket.getInputStream()
            val dataInputStream = DataInputStream(input)
            //데이터를 보내기 위한 코드
            val output = socket.getOutputStream()
            val dataOutputStream = DataOutputStream(output)
            //클라이언트로 데이터를 보내는 코드를 작성하였습니다.
            dataOutputStream.writeInt(7)
            dataOutputStream.writeUTF("서버 문자열")
            //그리고 클라이언트로부터 받은 데이터를 출력하는 코드를 작성하였습니다
            val intData = dataInputStream.readInt()
            println("Android 에서 받은 숫자 : $intData")
            //그리고 소켓과 서버를 차례대로 연결을 끊어주었습니다.
            socket.close()
            server.close()

            println("Android 에서 받은 숫자 : $intData")
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}