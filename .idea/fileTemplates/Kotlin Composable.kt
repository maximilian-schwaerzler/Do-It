#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ${NAME}(modifier: Modifier = Modifier) {
    #[[ $END$ ]]#
}