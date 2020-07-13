/*
 * Copyright (C) 2015 UNICEF Tanzania.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tz.co.rita.uiaids;

public class EnumeratedFields {
	enum SpinnerType{
		COUNTRY, SEX, BIRTH_PLACE, BIRTH_TYPE, BIRTH_STATE, CERTIFICATE
	}
	
	public enum sex {
		Choose(0), M(1), F(2);
		
		sex(int id){
			this.id = id;
		}
		private int id;
		
		public int getId(){
			return this.id;
		}
		
	}; // {MALE, FEMALE};

	public enum birthPlace {
		H(0), N(1), M(2);
		birthPlace(int id){
			this.id = id;
		}
		private int id;
		
		public int getId(){
			return this.id;
		}
	}; // {TREATMENT_CENTER, HOME, OTHER};

	public enum stateAtBirth {
		M(0), H(1);
		stateAtBirth(int id){
			this.id = id;
		}
		private int id;
		
		public int getId(){
			return this.id;
		}
	}; // {ALIVE, DEAD};
	
	public enum filledBy {
		M(0), B(1), T(2), Y(3);
		filledBy(int id){
			this.id = id;
		}
		private int id;
		
		public int getId(){
			return this.id;
		}
		
	}; // {MOTHER, FATHER, TREATMENT_CENTER_STAFF, OTHER};

	public enum certificateIssued {
		Y(0), N(1);
		certificateIssued(int id){
			this.id = id;
		}
		private int id;
		
		public int getId(){
			return this.id;
		}
	}; // {YES, NO}
	public enum typeOfBirth {
		S(0), T(1), M(2);
		typeOfBirth(int id){
			this.id = id;
		}
		private int id;
		
		public int getId(){
			return this.id;
		} //Single, Twins, More
	};

}
