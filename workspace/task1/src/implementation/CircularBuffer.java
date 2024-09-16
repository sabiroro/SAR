package implementation;

/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright: 2017
 *      Author: Pr. Olivier Gruber <olivier dot gruber at acm dot org>
 */

public class CircularBuffer {

	int m_capacity;
	int m_start, m_end;
	byte m_elements[];

	public CircularBuffer(int capacity) {
		m_capacity = capacity;
		m_elements = new byte[capacity];
		m_start = m_end = 0;
	}

	public boolean full() {
		int next = (m_end + 1) % m_capacity;
		return (next == m_start);
	}

	public boolean empty() {
		return (m_start == m_end);
	}

	/**
	 * Pushes a byte in the buffer, if the buffer is not full, throws an
	 * IllegalStateException otherwise.
	 *
	 * @return true if the push succeeded.
	 */
	public void push(byte elem) {
		int next = (m_end + 1) % m_capacity;
		if (next == m_start)
			throw new IllegalStateException("Full");
		m_elements[m_end] = elem;
		m_end = next;
	}

	/**
	 * @return the next available byte, if the buffer is not empty, throws an
	 *         IllegalStateException otherwise.
	 */
	public byte pull() {
		if (m_start != m_end) {
			int next = (m_start + 1) % m_capacity;
			byte elem = m_elements[m_start];
			m_start = next;
			return elem;
		}
		throw new IllegalStateException("Empty");
	}

}