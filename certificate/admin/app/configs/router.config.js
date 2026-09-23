import React from 'react'
import { browserHistory, Route, Router, Redirect } from 'react-router'
import { isLogin } from '@configs/common'

import * as base from '@pages/base' // 基础
import GradingWorkbench from '../pages/grading'
import GradingTemporary from '../pages/grading/Temporary'
import * as enter from '@pages/enter'
import * as preview from '@pages/preview'
import * as search from '@pages/search'
import * as managerCenter from '@pages/managerCenter'
import * as aboutus from '@pages/aboutus'
import * as contactus from '@pages/contactus'
import * as teach from '@pages/teach'
import * as smartdetect from '@pages/smartdetect'
import * as smartManage from '@pages/smartManage'

export default () => (
  <Router onUpdate={() => document.getElementById('root').scrollTo(0, 0)} history={browserHistory}>
    <Redirect from="/grading" to="/rateWorkflow" />
    <Route path="/" component={search.search} />
    <Route path="/manage" component={base.app} onEnter={isLogin}>
      {/* <IndexRoute component={managerCenter.preciousManage} /> */}
      <Route path="/preciousManage" component={managerCenter.preciousManage} />
      <Route path="/cartoonManage" component={managerCenter.cartoonManage} />
      <Route path="/rateManage" component={managerCenter.rateManage} />
      <Route path="/rateTemporary" component={GradingTemporary} />
      <Route path="/rateIntake" component={GradingWorkbench} />
      <Route path="/rateWorkflow" component={GradingWorkbench} />
      <Route path="/userManage" component={managerCenter.userManage} />
      <Route path="/changePwd" component={managerCenter.changePwd} />
      <Route path="/roleManage" component={managerCenter.roleManage} />
      <Route path="/operationManage" component={managerCenter.operationManage} />
      <Route path="/menuManage" component={managerCenter.menuManage} />
      <Route path="/smartUserManage" component={smartManage.smartUserManage} />
      <Route path="/smartAuditManage" component={smartManage.smartAuditManage} />
      <Route path="/smartScoreManage" component={smartManage.smartScoreManage} />
      <Route path="/smartBrandManage" component={smartManage.smartBrandManage} />
      <Route path="/smartCardManage" component={smartManage.smartCardManage} />
      <Route path="/smartConfigManage" component={smartManage.smartConfigManage} />
    </Route>
    <Route path="/login" component={base.login} />
    {/* <Route path="/welcome" component={base.welcome} /> */}
    {/* <Route path="/developing" component={base.developing} /> */}
    <Route path="/home" component={enter.home} />
    <Route path="/preview" component={preview.preview} />
    <Route path="/aboutus" component={aboutus.aboutus} />
    <Route path="/contactus" component={contactus.contactus} />
    <Route path="/teach" component={teach.teach} />
    <Route path="/collect" component={teach.collect} />
    <Route path="/sailulu" component={teach.sailulu} />
    <Route path="/letterworm" component={teach.letterworm} />
    <Route path="/diy" component={teach.diy} />
    <Route path="/cardstone" component={teach.cardstone} />
    <Route path="/cardstar" component={teach.cardstar} />
    <Route path="/cardstarExchange" component={teach.cardstarexchange} />
    <Route path="/cardstarStand" component={teach.cardstarstand} />
    <Route path="/cardstarReGrade" component={teach.cardstarregrade} />
    <Route path="/smartHome" component={smartdetect.home} />
    <Route path="*" component={base.notfound} />
  </Router>
)
