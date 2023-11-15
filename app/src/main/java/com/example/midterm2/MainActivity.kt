import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.midterm2.ApiService
import com.example.midterm2.User
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import retrofit2.http.GET

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        setContent {
            AppUI(viewModel = viewModel)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppUI(viewModel: UserViewModel) {
        viewModel.users.observe(this) {
            setContent {
                Column {
                    TopAppBar(
                        title = { Text(text = "User List") }
                    )
                    if (it.isNullOrEmpty()) {
                        Text(text = "Loading users...")
                    } else {
                        UserList(users = it)
                    }
                }
            }
        }
    }
}

class UserViewModel : ViewModel() {
    private val userService = RetrofitClient.getUsers

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    init {
        viewModelScope.launch {
            try {
                val response = userService.getUsers()
                _users.value = response
            } catch (e: Exception) {
                Log.e("Retrofit", "Error fetching user data", e)
            }
        }
    }
}

object RetrofitClient {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val getUsers: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

@Composable
fun UserList(userViewModel: UserViewModel = viewModel()) {
    val users by remember<List<User>> { userViewModel.users }

    if (users.isEmpty()) {
        Text("Error fetching data")
    } else {
        LazyColumn {
            items(users) { user ->
                UserListItem(user = user)
            }
        }
    }
}

@Composable
fun UserListItem(user: Int) {
    Text(text = "${user.name} ${user.username}")
}