package pe.novatick.anitec.features.linking.presentation
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.navigation.compose.*
import pe.novatick.anitec.R
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.*
import pe.novatick.anitec.features.livestock.presentation.*
import pe.novatick.anitec.ui.components.*
import pe.novatick.anitec.ui.navigation.VeterinarianTab
import pe.novatick.anitec.ui.theme.*

@Composable internal fun LinkError(error: AuthFailure) {
 val res=when {
  error.code in listOf("INVITATION_ALREADY_EXISTS","ALREADY_LINKED")->R.string.lk_duplicate
  error.code=="LINKING_LIMIT_REACHED"->R.string.lk_limit
  error.code=="RECIPIENT_NOT_REGISTERED"->R.string.lk_recipient
  error.code=="INVITATION_EXPIRED" || error.status==409->R.string.lk_conflict
  error.status==401 || error.problem in listOf(AuthProblem.UNAUTHORIZED,AuthProblem.RELOGIN)->R.string.lk_session
  error.status==403->R.string.lk_denied
  error.status==404->R.string.lk_not_found
  error.status==429->R.string.lk_retry_after
  error.problem==AuthProblem.NETWORK->R.string.lk_network
  error.status==400->R.string.lk_email_error
  else->R.string.lk_error
 }
 LsNotice(stringResource(res),true)
}
@Composable private fun LinkFeedback(s: LinkingState,vm: LinkingViewModel){
 s.error?.let {LinkError(it)}
 if(s.uncertain){LsNotice(stringResource(R.string.lk_uncertain),true);TextButton(onClick=vm::acknowledgeUncertain,enabled=!s.busy){LsText(stringResource(R.string.lk_ack),size=11,color=PrimaryGreen)}}
 s.notice?.let {val res=when(it){"sent"->R.string.lk_sent;"unconfirmed"->R.string.lk_unconfirmed;"accepted"->R.string.lk_accepted;"rejected"->R.string.lk_rejected;else->R.string.lk_revoked};LsNotice(stringResource(res))}
 if(s.busy)LsLoading(stringResource(R.string.lk_loading))
}
@Composable private fun <T> LinkReadContent(read: LinkRead<T>,vm: LinkingViewModel,content: @Composable (T)->Unit){
 if(read.loading)LsLoading(stringResource(R.string.lk_loading))
 read.error?.let {LinkError(it);TextButton(onClick=vm::refresh){LsText(stringResource(R.string.lk_reload),color=PrimaryGreen)}}
 read.value?.let {content(it)}
}
@Composable private fun LinkPage(title: String,subtitle: String,content: @Composable ColumnScope.()->Unit){
 Column(Modifier.fillMaxSize().background(Color.White).imePadding().verticalScroll(rememberScrollState())){
  Box(Modifier.fillMaxWidth().height(30.dp).clip(CattleHeaderShape).background(PrimaryGreenAccent))
  Column(Modifier.padding(horizontal=28.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
   Text(title,fontFamily=Poppins,fontSize=24.sp,fontWeight=androidx.compose.ui.text.font.FontWeight.Bold,color=PrimaryGreen)
   LsText(subtitle,color=NeutralSlate);content()
  }
 }
}
@Composable private fun LinkAvatar(name: String,size: Int=42){
 val initials=name.split(Regex("\\s+")).filter(String::isNotBlank).take(2).mapNotNull {it.firstOrNull()}.joinToString("").uppercase()
 Box(Modifier.size(size.dp).background(PrimaryGreenAccent,CircleShape),contentAlignment=Alignment.Center){LsText(initials,size=if(size>42)14 else 11,bold=true,color=Color.White)}
}
@Composable private fun LinkCard(onClick: (()->Unit)?=null,content: @Composable RowScope.()->Unit){
 Surface(Modifier.fillMaxWidth().then(if(onClick!=null)Modifier.clickable(onClick=onClick) else Modifier),shape=RoundedCornerShape(15.dp),color=Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border)){
  Row(Modifier.padding(12.dp).heightIn(min=44.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp),content=content)
 }
}
@Composable private fun linkDate(value: String): String=formatObservationDate(value.take(10),LocalConfiguration.current.locales[0])
@Composable fun InviteVetScreen(vm: LinkingViewModel,back: ()->Unit){
 val s by vm.state.collectAsState();var email by remember {mutableStateOf("")};var touched by remember {mutableStateOf(false)}
 val language=LocalConfiguration.current.locales[0].language
 BackHandler(!s.busy){back()}
 AnimalPage(stringResource(R.string.lk_invite),back,busy=s.busy,subtitle=stringResource(R.string.lk_invite_sub),pinnedHeader=true){
  Box(Modifier.fillMaxWidth().height(124.dp),contentAlignment=Alignment.Center){
   Box(Modifier.size(108.dp).background(LivestockVisuals.Mint,CircleShape),contentAlignment=Alignment.Center){
    Box(Modifier.offset(x=(-4).dp,y=(-4).dp).rotate(-7f).size(61.dp,50.dp).background(PrimaryGreenAccent,RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Icon(painterResource(R.drawable.link_mail_official),null,tint=Color.Unspecified,modifier=Modifier.size(38.dp))}
    Box(Modifier.offset(x=36.dp,y=20.dp).rotate(8f).size(47.dp).shadow(5.dp,RoundedCornerShape(15.dp)).background(Color.White,RoundedCornerShape(15.dp)),contentAlignment=Alignment.Center){Icon(painterResource(R.drawable.link_stethoscope_official),null,tint=Color.Unspecified,modifier=Modifier.size(32.dp))}
   }
  }
  LsText(stringResource(R.string.lk_email)+" *",size=12,bold=true)
  TextField(email,{email=it;touched=true},enabled=!s.busy && s.sent==null && !s.uncertain,singleLine=true,placeholder={LsText(stringResource(R.string.lk_email_hint),size=12,color=NeutralSlate)},
   keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),textStyle=TextStyle(fontFamily=Poppins,fontSize=13.sp),shape=RoundedCornerShape(13.dp),
   colors=TextFieldDefaults.colors(focusedContainerColor=LivestockVisuals.Pale,unfocusedContainerColor=LivestockVisuals.Pale,focusedIndicatorColor=Color.Transparent,unfocusedIndicatorColor=Color.Transparent),modifier=Modifier.fillMaxWidth().testTag("link_email"))
  if(touched && !validVetEmail(email))LsText(stringResource(R.string.lk_email_error),size=11,color=Color(0xFFAE4141))
  Row(Modifier.fillMaxWidth().background(Color(0xFFF4FCF7),RoundedCornerShape(13.dp)).padding(11.dp),horizontalArrangement=Arrangement.spacedBy(9.dp)){
   Icon(painterResource(R.drawable.link_shield_official),null,tint=Color.Unspecified,modifier=Modifier.size(20.dp));LsText(stringResource(R.string.lk_safety),Modifier.weight(1f),size=10,color=LivestockVisuals.Secondary)
  }
  LinkFeedback(s,vm)
  LsAction(stringResource(R.string.lk_send),{touched=true;vm.invite(email,language)},Modifier.fillMaxWidth().testTag("link_send"),primary=true,enabled=validVetEmail(email) && !s.busy && !s.uncertain && s.sent==null)
  if(s.sent!=null) LsAction(stringResource(R.string.lk_vets),back,Modifier.fillMaxWidth())
 }
}
@Composable fun RancherLinksScreen(vm: LinkingViewModel,invite: ()->Unit){
 val s by vm.state.collectAsState();var remove by remember {mutableStateOf<VetLink?>(null)}
 LinkPage(stringResource(R.string.lk_vets),stringResource(R.string.lk_vets_sub)){
  LsAction(stringResource(R.string.lk_invite),{vm.newInvite();invite()},Modifier.fillMaxWidth(),primary=true,enabled=!s.busy,icon=R.drawable.ls_mail_official)
  LinkFeedback(s,vm)
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){LsText(stringResource(R.string.lk_linked),Modifier.weight(1f),bold=true);TextButton(onClick=vm::refresh,enabled=!s.busy){LsText(stringResource(R.string.lk_reload),size=11,color=PrimaryGreen)}}
  LinkReadContent(s.links,vm){links->
   if(links.isEmpty())LsNotice(stringResource(R.string.lk_no_links))
   links.forEach {link->LinkCard {
    LinkAvatar(link.vetName);Column(Modifier.weight(1f)){LsText(link.vetName,size=12,bold=true);LsText(stringResource(R.string.lk_since,linkDate(link.linkedAt)),size=10,color=NeutralSlate)}
    OutlinedButton(onClick={remove=link},enabled=!s.busy && !s.uncertain,shape=RoundedCornerShape(8.dp),contentPadding=PaddingValues(8.dp),border=BorderStroke(1.dp,Color(0xFFDF6262))){LsText(stringResource(R.string.lk_remove),size=10,color=Color(0xFFDF6262))}
   }}
  }
  s.sent?.let {sent->LsText(stringResource(R.string.lk_sent_receipt),bold=true);LsNotice(sent.email+" · "+stringResource(R.string.lk_pending_status))}
 }
 remove?.let {link->LsModal(stringResource(R.string.lk_remove_title),{if(!s.busy)remove=null},content={LsText(link.vetName,bold=true);LsText(stringResource(R.string.lk_remove_info))},actions={
  LsAction(stringResource(R.string.lk_cancel),{remove=null},enabled=!s.busy)
  LsAction(stringResource(R.string.lk_remove),{remove=null;vm.revoke(link.id)},primary=true,enabled=!s.busy && !s.uncertain)
 })}
}
@Composable private fun PendingCards(s: LinkingState,vm: LinkingViewModel){
 LinkReadContent(s.pending,vm){items->
  if(items.isEmpty())LsNotice(stringResource(R.string.lk_no_requests))
  items.forEach {item->Surface(shape=RoundedCornerShape(17.dp),border=BorderStroke(1.dp,LivestockVisuals.Border),color=Color.White,modifier=Modifier.fillMaxWidth()){
   Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){LinkAvatar(item.farmerName,58);Column(Modifier.weight(1f)){
     LsText(item.farmerName,size=12,bold=true);LsText(stringResource(R.string.lk_date,linkDate(item.sentDate)),size=10,color=NeutralSlate);LsText(stringResource(R.string.lk_request_detail),size=10,color=LivestockVisuals.Secondary)
    }}
    Row(horizontalArrangement=Arrangement.spacedBy(9.dp)){
     LsAction(stringResource(R.string.lk_reject),{vm.respond(item.id,false)},Modifier.weight(1f).testTag("link_reject_"+item.id),enabled=!s.busy && !s.uncertain)
     LsAction(stringResource(R.string.lk_accept),{vm.respond(item.id,true)},Modifier.weight(1f).testTag("link_accept_"+item.id),primary=true,enabled=!s.busy && !s.uncertain)
    }
   }
  }}
 }
}
@Composable fun VeterinarianFarmersScreen(vm: LinkingViewModel,detail: (String)->Unit,invitations: ()->Unit){
 val s by vm.state.collectAsState();var search by remember {mutableStateOf("")}
 LinkPage(stringResource(R.string.lk_farmers),stringResource(R.string.lk_active)){
  BasicTextField(search,{search=it},singleLine=true,textStyle=TextStyle(fontFamily=Poppins,fontSize=13.sp,color=LivestockVisuals.Ink),modifier=Modifier.fillMaxWidth().testTag("link_search"),decorationBox={editor->Row(Modifier.fillMaxWidth().heightIn(min=46.dp).background(LivestockVisuals.Pale,RoundedCornerShape(14.dp)).padding(14.dp),horizontalArrangement=Arrangement.spacedBy(9.dp)){
   Icon(painterResource(R.drawable.ic_search),null,Modifier.size(20.dp),tint=NeutralSlate);Box(Modifier.weight(1f)){if(search.isEmpty())LsText(stringResource(R.string.lk_search),color=NeutralSlate);editor()}
  }})
  LinkFeedback(s,vm)
  LinkReadContent(s.capacity,vm){LsText(stringResource(R.string.lk_capacity,it.active,it.allowed),size=11,color=LivestockVisuals.Secondary)}
  TextButton(onClick=vm::refresh,enabled=!s.busy){LsText(stringResource(R.string.lk_reload),color=PrimaryGreen)}
  LsText(stringResource(R.string.lk_active),bold=true)
  LinkReadContent(s.farmers,vm){farmers->val found=farmers.filter {it.name.contains(search,true)}
   if(farmers.isEmpty())LsNotice(stringResource(R.string.lk_no_links)) else if(found.isEmpty())LsNotice(stringResource(R.string.lk_no_match))
   found.forEach {farmer->LinkCard({detail(farmer.id)}){
    LinkAvatar(farmer.name);Column(Modifier.weight(1f)){LsText(farmer.name,size=12,bold=true);LsText(stringResource(R.string.lk_counts,farmer.animals,farmer.farms.size),size=10,color=NeutralSlate)}
    Icon(painterResource(R.drawable.ls_chevron_official),null,tint=Color.Unspecified,modifier=Modifier.size(8.25.dp,14.5.dp))
   }}
  }
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){LsText(stringResource(R.string.lk_pending),Modifier.weight(1f),bold=true);TextButton(onClick=invitations){LsText(stringResource(R.string.lk_all),size=11,color=PrimaryGreen)}}
  PendingCards(s,vm)
 }
}
@Composable fun VeterinarianInvitationsScreen(vm: LinkingViewModel,back: ()->Unit){
 val s by vm.state.collectAsState();AnimalPage(stringResource(R.string.lk_invitations),back,busy=s.busy,subtitle=stringResource(R.string.lk_requests)){
  LinkFeedback(s,vm);TextButton(onClick=vm::refresh,enabled=!s.busy){LsText(stringResource(R.string.lk_reload),color=PrimaryGreen)};PendingCards(s,vm)
 }
}
@Composable fun LinkedFarmerDetailScreen(vm: LinkingViewModel,id: String,back: ()->Unit){
 val s by vm.state.collectAsState();val farmer=s.farmers.value?.find {it.id==id};val link=s.links.value?.find {it.farmerId==id}
 LaunchedEffect(id){vm.refresh()}
 AnimalPage(farmer?.name ?: stringResource(R.string.lk_farmers),back,subtitle=stringResource(R.string.lk_farmer_sub)){
  LinkReadContent(s.farmers,vm){
   if(farmer==null)LsNotice(stringResource(R.string.lk_detail_lost),true) else {
    LinkCard {LinkAvatar(farmer.name,50);Column(Modifier.weight(1f)){LsText(farmer.name,bold=true);link?.let {LsText(stringResource(R.string.lk_since,linkDate(it.linkedAt)),size=10,color=NeutralSlate)};LsText(stringResource(R.string.lk_active),size=10,color=PrimaryGreen)}}
    LsText(stringResource(R.string.lk_farms),bold=true)
    if(farmer.farms.isEmpty())LsNotice(stringResource(R.string.lk_no_farms))
    farmer.farms.forEach {farm->LinkCard {Icon(painterResource(R.drawable.ic_home),null,tint=PrimaryGreen,modifier=Modifier.size(24.dp));Column {LsText(farm.name,bold=true);LsText(stringResource(R.string.lk_farm_animals,farm.animals),size=11,color=NeutralSlate)}}}
   }
  }
  TextButton(onClick=vm::refresh){LsText(stringResource(R.string.lk_reload),color=PrimaryGreen)}
 }
}
@Composable fun VeterinaryLinkingShell(repo: LinkingRepository,profile: Profile,logout: ()->Unit){
 val vm=remember(repo,profile.id){LinkingViewModel(repo,profile.role)}
 DisposableEffect(vm){vm.refresh();onDispose{vm.close()}}
 val nav=rememberNavController();val entry by nav.currentBackStackEntryAsState();val route=entry?.destination?.route
 val tab=when(route){"home"->VeterinarianTab.HOME;"profile"->VeterinarianTab.PROFILE;"agenda"->VeterinarianTab.AGENDA;else->VeterinarianTab.RANCHERS}
 val s by vm.state.collectAsState()
 val labels=mapOf(VeterinarianTab.HOME to stringResource(R.string.lk_home),VeterinarianTab.AGENDA to stringResource(R.string.lk_agenda),VeterinarianTab.RANCHERS to stringResource(R.string.lk_farmers),VeterinarianTab.PROFILE to stringResource(R.string.lk_profile))
 Scaffold(containerColor=Color.White,bottomBar={VeterinarianBottomBar(tab,{selected->if(!s.busy)nav.navigate(when(selected){VeterinarianTab.HOME->"home";VeterinarianTab.PROFILE->"profile";VeterinarianTab.AGENDA->"agenda";else->"farmers"}){launchSingleTop=true;popUpTo("farmers")}},labels)}){padding->Box(Modifier.padding(padding).consumeWindowInsets(padding)){
  NavHost(nav,startDestination="farmers"){
   composable("home"){LinkPage(profile.name+" "+profile.lastName,stringResource(R.string.lk_home)){LsAction(stringResource(R.string.lk_farmers),{nav.navigate("farmers")},Modifier.fillMaxWidth(),primary=true);LsAction(stringResource(R.string.lk_invitations),{nav.navigate("invitations")},Modifier.fillMaxWidth())}}
   composable("farmers"){VeterinarianFarmersScreen(vm,{nav.navigate("farmer/$it")},{nav.navigate("invitations")})}
   composable("invitations"){VeterinarianInvitationsScreen(vm){nav.popBackStack()}}
   composable("farmer/{id}"){LinkedFarmerDetailScreen(vm,it.arguments?.getString("id").orEmpty()){nav.popBackStack()}}
   composable("profile"){pe.novatick.anitec.ui.screens.home.RancherProfileScreen(userName=profile.name+" "+profile.lastName,userEmail=profile.email,onLogoutClick=logout)}
   composable("agenda"){LinkPage(stringResource(R.string.lk_agenda),stringResource(R.string.lk_clinical_later)) {}}
  }
 }}
}
