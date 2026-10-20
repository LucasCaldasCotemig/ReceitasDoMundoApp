package com.cotemig.receitas.data

import android.content.Context
import com.cotemig.receitas.network.RetrofitInstance

class AppContainer private constructor(context: Context) {

    private val banco = AppDatabase.criar(context)

    val usuarios = UsuarioRepository(banco.usuarioDao(), SessaoStorage(context))
    val favoritos = FavoritoRepository(banco.favoritoDao())
    val receitas = ReceitaRepository(RetrofitInstance.api, TradutorMlKit())

    companion object {
        @Volatile
        private var instancia: AppContainer? = null

        fun obter(context: Context): AppContainer =
            instancia ?: synchronized(this) {
                instancia ?: AppContainer(context.applicationContext).also { instancia = it }
            }
    }
}
