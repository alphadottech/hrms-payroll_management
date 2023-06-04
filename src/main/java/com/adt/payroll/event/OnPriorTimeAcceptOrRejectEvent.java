/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adt.payroll.event;

import java.util.Optional;

import org.springframework.context.ApplicationEvent;

import com.adt.payroll.model.Priortime;

public class OnPriorTimeAcceptOrRejectEvent extends ApplicationEvent {

	private Optional<Priortime> priortime;
	private String action;
	private String actionStatus;

	public OnPriorTimeAcceptOrRejectEvent(Optional<Priortime> priortime, String action, String actionStatus) {
		super(priortime);
		this.priortime = priortime;
		this.action = action;
		this.actionStatus = actionStatus;
	}

	public String getAction() {
		return action;
	}

	public Optional<Priortime> getPriortime() {
		return priortime;
	}

	public void setPriortime(Optional<Priortime> priortime) {
		this.priortime = priortime;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getActionStatus() {
		return actionStatus;
	}

	public void setActionStatus(String actionStatus) {
		this.actionStatus = actionStatus;
	}
}
