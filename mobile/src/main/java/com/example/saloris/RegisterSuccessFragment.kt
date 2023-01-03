package world.saloris.donoff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.R
import com.example.saloris.databinding.FragmentRegisterSuccessBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class RegisterSuccessFragment : Fragment() {

    private lateinit var binding: FragmentRegisterSuccessBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterSuccessBinding.inflate(layoutInflater, container, false)

        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        // 로그인하기 -> 로그인
        binding.btnLogin.setOnClickListener {
            navController.navigate(R.id.action_registerSuccessFragment_to_loginFragment, arguments)
        }
    }
}